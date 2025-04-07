from datetime import datetime, timedelta
import random
import pandas as pd
import numpy as np
from weather_ml import WeatherPredictor
import queue

class WeatherStation:
    def __init__(self):
        self.cities = {
            "New York": {"lat": 40.7128, "lon": -74.0060},
            "London": {"lat": 51.5074, "lon": -0.1278},
            "Tokyo": {"lat": 35.6762, "lon": 139.6503},
            "Paris": {"lat": 48.8566, "lon": 2.3522},
            "Sydney": {"lat": -33.8688, "lon": 151.2093}
        }
        
        self.weather_conditions = [
            "Sunny", "Partly Cloudy", "Cloudy", "Rain", 
            "Thunderstorm", "Clear", "Overcast", "Light Rain"
        ]
        
        # Initialize the ML model
        self.predictor = WeatherPredictor.load_model('weather_model.joblib')
        
        # Queue for storing historical data
        self.history_queue = queue.Queue(maxsize=1000)

        # Store current weather state for each city
        self.current_states = {}
        self.initialize_weather_states()

        # Constants for weather changes
        self.MAX_TEMP_CHANGE = 0.2  # Maximum temperature change per 5 seconds
        self.MAX_HUMIDITY_CHANGE = 1  # Maximum humidity change per 5 seconds
        self.MAX_WIND_CHANGE = 0.5   # Maximum wind speed change per 5 seconds
        self.MAX_PRESSURE_CHANGE = 0.2  # Maximum pressure change per 5 seconds
        
        # Condition change probability (every 5 seconds)
        self.CONDITION_CHANGE_PROBABILITY = 0.02  # 2% chance to change condition
        
        # Weather condition transition rules
        self.condition_transitions = {
            "Sunny": ["Partly Cloudy", "Clear"],
            "Partly Cloudy": ["Sunny", "Cloudy", "Clear"],
            "Cloudy": ["Partly Cloudy", "Overcast", "Light Rain"],
            "Light Rain": ["Cloudy", "Rain", "Overcast"],
            "Rain": ["Light Rain", "Thunderstorm", "Overcast"],
            "Thunderstorm": ["Rain", "Overcast"],
            "Clear": ["Partly Cloudy", "Sunny"],
            "Overcast": ["Cloudy", "Light Rain"]
        }

    def initialize_weather_states(self):
        """Initialize weather states for all cities"""
        for city in self.cities:
            if city not in self.current_states:
                self.current_states[city] = self.generate_initial_weather(city)

    def generate_initial_weather(self, city):
        """Generate initial weather state for a city"""
        base_temps = {
            "New York": (15, 30),
            "London": (10, 25),
            "Tokyo": (18, 32),
            "Paris": (12, 28),
            "Sydney": (20, 35)
        }
        min_temp, max_temp = base_temps.get(city, (15, 30))
        
        return {
            "city": city,
            "coordinates": self.cities[city],
            "temperature": {
                "celsius": round(random.uniform(min_temp, max_temp), 1),
                "fahrenheit": round((random.uniform(min_temp, max_temp) * 9/5) + 32, 1)
            },
            "humidity": random.randint(30, 90),
            "wind_speed": {
                "kph": round(random.uniform(0, 30), 1),
                "mph": round(random.uniform(0, 30) * 0.621371, 1)
            },
            "pressure": random.randint(980, 1025),
            "condition": random.choice(self.weather_conditions)
        }

    def get_next_condition(self, current_condition):
        """Get next weather condition based on transition rules"""
        if random.random() < self.CONDITION_CHANGE_PROBABILITY:
            possible_conditions = self.condition_transitions.get(current_condition, self.weather_conditions)
            return random.choice(possible_conditions)
        return current_condition

    def gradual_change(self, current, target, max_change):
        """Calculate gradual change between current and target values"""
        diff = target - current
        if abs(diff) > max_change:
            return current + (max_change if diff > 0 else -max_change)
        return target

    def generate_weather_data(self, city):
        """Generate weather data with smooth transitions"""
        current_state = self.current_states[city]
        
        # Get current time
        current_time = datetime.utcnow()
        
        # Calculate target values based on time of day and season
        hour = current_time.hour
        day_of_year = current_time.timetuple().tm_yday
        
        # Base temperature with daily and seasonal variations
        seasonal_factor = np.sin(2 * np.pi * day_of_year / 365)  # -1 to 1
        daily_factor = np.sin(2 * np.pi * (hour + current_time.minute/60) / 24)  # -1 to 1
        
        base_temps = {
            "New York": (15, 30),
            "London": (10, 25),
            "Tokyo": (18, 32),
            "Paris": (12, 28),
            "Sydney": (20, 35)
        }
        min_temp, max_temp = base_temps.get(city, (15, 30))
        mid_temp = (min_temp + max_temp) / 2
        
        target_temp = mid_temp + (5 * seasonal_factor) + (3 * daily_factor)
        
        # Generate new weather state with smooth transitions
        new_temp_celsius = self.gradual_change(
            current_state["temperature"]["celsius"],
            target_temp,
            self.MAX_TEMP_CHANGE
        )
        
        new_humidity = self.gradual_change(
            current_state["humidity"],
            current_state["humidity"] + random.uniform(-2, 2),
            self.MAX_HUMIDITY_CHANGE
        )
        new_humidity = max(30, min(90, new_humidity))
        
        new_wind = self.gradual_change(
            current_state["wind_speed"]["kph"],
            current_state["wind_speed"]["kph"] + random.uniform(-1, 1),
            self.MAX_WIND_CHANGE
        )
        new_wind = max(0, new_wind)
        
        new_pressure = self.gradual_change(
            current_state["pressure"],
            current_state["pressure"] + random.uniform(-0.5, 0.5),
            self.MAX_PRESSURE_CHANGE
        )
        new_pressure = max(980, min(1025, round(new_pressure)))
        
        # Update weather condition based on transition rules
        new_condition = self.get_next_condition(current_state["condition"])
        
        # Create new weather state
        new_state = {
            "city": city,
            "coordinates": self.cities[city],
            "timestamp": current_time.strftime("%Y-%m-%d %H:%M:%S"),
            "temperature": {
                "celsius": round(new_temp_celsius, 1),
                "fahrenheit": round((new_temp_celsius * 9/5) + 32, 1)
            },
            "humidity": round(new_humidity),
            "wind_speed": {
                "kph": round(new_wind, 1),
                "mph": round(new_wind * 0.621371, 1)
            },
            "pressure": new_pressure,
            "condition": new_condition
        }
        
        # Update current state
        self.current_states[city] = new_state
        
        return new_state

    def predict_next_weather(self, current_data):
        """Predict weather with minimal variations"""
        features = pd.DataFrame({
            'hour': [datetime.utcnow().hour],
            'day': [datetime.utcnow().day],
            'month': [datetime.utcnow().month],
            'day_of_week': [datetime.utcnow().weekday()],
            'humidity': [current_data['humidity']],
            'wind_speed': [current_data['wind_speed']['kph']],
            'pressure': [current_data['pressure']],
            'condition_encoded': self.predictor.label_encoder.transform([current_data['condition']])
        })
        
        # Get base prediction
        predicted_temp = self.predictor.predict(features)[0]
        
        # Limit the prediction change
        current_temp = current_data['temperature']['celsius']
        max_change = self.MAX_TEMP_CHANGE * 2  # Allow slightly larger changes for predictions
        
        if abs(predicted_temp - current_temp) > max_change:
            predicted_temp = current_temp + (max_change if predicted_temp > current_temp else -max_change)
        
        # Create prediction
        prediction = current_data.copy()
        prediction['timestamp'] = (datetime.utcnow() + timedelta(seconds=5)).strftime("%Y-%m-%d %H:%M:%S")
        prediction['temperature']['celsius'] = round(predicted_temp, 1)
        prediction['temperature']['fahrenheit'] = round((predicted_temp * 9/5) + 32, 1)
        prediction['predicted'] = True
        
        return prediction