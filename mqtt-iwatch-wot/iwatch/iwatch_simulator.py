import numpy as np
from scipy.signal import butter, lfilter
import time
from datetime import datetime

def iwatch(dict_dt):
    # Define input parameters
    sampling_rate = 100  # Hz
    duration = 1  # second
    min_heart_rate = 60  # bpm
    max_heart_rate = 100  # bpm
    amplitude = 1  # arbitrary units
    breathing_factor = 0.2  # scale factor for breathing rate
    activity_factor = 0.3  # scale factor for physical activity
    stress_factor = 0.1  # scale factor for stress and emotions
    age_factor = 0.05  # scale factor for age and gender

    # Compute filter coefficients
    nyquist_rate = sampling_rate/2  # Nyquist rate
    cutoff_freq = 20  # Hz
    cutoff_freq_norm = cutoff_freq/nyquist_rate
    b, a = butter(2, cutoff_freq_norm, btype='low')

    # Define initial location
    latitude = 37.7749
    longitude = -122.4194
    delta_lat = 0.00001  # change in latitude per second (approximately 1 meters per second)
    delta_long = 0.00001  # change in longitude per second (approximately 1 meters per second)

    # Start generating data
    t_start = time.time()
    while True:
        t = np.arange(0, duration, 1/sampling_rate)

        # Generate random heart rate within a range
        heart_rate = np.random.randint(min_heart_rate, max_heart_rate+1)

        # Add variability based on breathing rate
        breathing_rate = np.random.normal(15, 5)  # breaths per minute
        breathing_scale = 1 + breathing_factor * np.sin(2*np.pi*breathing_rate/60*t)
        x = amplitude * breathing_scale * np.sin(2*np.pi*heart_rate/60*t)

        # Add variability based on physical activity
        activity_level = np.random.normal(1, 0.2)  # arbitrary units
        activity_scale = 1 + activity_factor * np.random.rand() * activity_level
        x = x * activity_scale

        # Add variability based on stress and emotions
        stress_scale = 1 + stress_factor * np.random.rand()
        x = x * stress_scale

        # Add variability based on age and gender
        age_scale = 1 + age_factor * np.random.normal(0, 1)
        gender_scale = 1 + age_factor * np.random.rand()
        x = x * age_scale * gender_scale

        # Get timestamp
        timestamp = str(datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f'))

        # Update location based on the change in latitude and longitude
        latitude += delta_lat * duration
        longitude += delta_long * duration

        dict_dt['timestamp'] = timestamp
        dict_dt['latitude'] = latitude
        dict_dt['longitude'] = longitude
        dict_dt['heart_rate'] = heart_rate

        # Print timestamp, location, and some statistics about the signal
        #print(dict_dt)

        # Wait until the end of the duration
        t_elapsed = time.time() - t_start
        time.sleep(duration - t_elapsed % duration)

        yield dict_dt
