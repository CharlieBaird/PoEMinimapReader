import matplotlib.pyplot as plt
import numpy as np
from sklearn.linear_model import LinearRegression

# 1402 x 790 image

# Your data
data = [
    (690, 98, 657.0, 341.0),
    (704, 434, 666.0, 386.0),
    (611, 652, 660.0, 408.0),
    (795, 965, 681.0, 432.0),
    (1007, 251, 699.0, 363.0),
    (1060, 452, 705.0, 388.0),
    (1279, 300, 733.0, 369.0),
    (1345, 714, 731.0, 413.0),
    (1587, 433, 768.0, 386.0),
    (271, 113, 626.0, 352.0),
    (1027, 10, 703.0, 337.0)
]

data = [(x2 - 701, y2 - 345, x1 - 1920/2, y1 - 1080/2) for (x1, y1, x2, y2) in data]

print(data)

# Extract X and Y components
x1 = np.array([[x] for x, _, _, _ in data])
x2 = np.array([x_ for _, _, x_, _ in data])

y1 = np.array([[y] for _, y, _, _ in data])
y2 = np.array([y_ for _, _, _, y_ in data])

# Fit models
model_x = LinearRegression().fit(x1, x2)
model_y = LinearRegression().fit(y1, y2)

# Get predictions
x2_pred = model_x.predict(x1)
y2_pred = model_y.predict(y1)

x_input = np.array([[624-701]])  # Must be 2D for sklearn
y_input = np.array([[346-345]])

# Predict using the trained models
predicted_x2 = model_x.predict(x_input)[0] + 1920/2
predicted_y2 = model_y.predict(y_input)[0] + 1080/2

print(f"Predicted X': {predicted_x2:.2f}")
print(f"Predicted Y': {predicted_y2:.2f}")

# Get equation components
slope_x = model_x.coef_[0]
intercept_x = model_x.intercept_
slope_y = model_y.coef_[0]
intercept_y = model_y.intercept_

print(f"X' = {slope_x:.4f} * X + {intercept_x:.4f}")
print(f"Y' = {slope_y:.4f} * Y + {intercept_y:.4f}")

# Plot
plt.figure(figsize=(12, 5))

# X plot
plt.subplot(1, 2, 1)
plt.scatter(x1, x2, color='blue', label="Actual X'")
plt.plot(x1, x2_pred, color='red', label="Predicted X'")
plt.xlabel("X")
plt.ylabel("X'")
plt.title("X → X' Linear Regression")
plt.legend()

# Y plot
plt.subplot(1, 2, 2)
plt.scatter(y1, y2, color='green', label="Actual Y'")
plt.plot(y1, y2_pred, color='red', label="Predicted Y'")
plt.xlabel("Y")
plt.ylabel("Y'")
plt.title("Y → Y' Linear Regression")
plt.legend()

plt.tight_layout()
plt.show()