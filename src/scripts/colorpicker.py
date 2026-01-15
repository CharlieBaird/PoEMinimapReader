import cv2
import numpy as np

def nothing(x):
    pass

# Load image
image = cv2.imread('../../samples/output/inventory.png')

if image is None:
    raise ValueError("Image failed to load. Check the file path.")

# Resize if image is very small (optional)
min_size = 100  # adjust as needed
height, width = image.shape[:2]
if height < min_size or width < min_size:
    scale = max(min_size / height, min_size / width)
    image = cv2.resize(image, (int(width * scale), int(height * scale)), interpolation=cv2.INTER_NEAREST)

# Create a window
cv2.namedWindow('image', cv2.WINDOW_NORMAL)  # allows resizing manually

# Trackbars
cv2.createTrackbar('HMin', 'image', 0, 179, nothing)
cv2.createTrackbar('SMin', 'image', 0, 255, nothing)
cv2.createTrackbar('VMin', 'image', 0, 255, nothing)
cv2.createTrackbar('HMax', 'image', 179, 179, nothing)
cv2.createTrackbar('SMax', 'image', 255, 255, nothing)
cv2.createTrackbar('VMax', 'image', 255, 255, nothing)

# Previous values
phMin = psMin = pvMin = phMax = psMax = pvMax = -1

while True:
    # Get current positions of all trackbars
    hMin = cv2.getTrackbarPos('HMin', 'image')
    sMin = cv2.getTrackbarPos('SMin', 'image')
    vMin = cv2.getTrackbarPos('VMin', 'image')
    hMax = cv2.getTrackbarPos('HMax', 'image')
    sMax = cv2.getTrackbarPos('SMax', 'image')
    vMax = cv2.getTrackbarPos('VMax', 'image')

    # Thresholding
    lower = np.array([hMin, sMin, vMin])
    upper = np.array([hMax, sMax, vMax])
    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    mask = cv2.inRange(hsv, lower, upper)
    result = cv2.bitwise_and(image, image, mask=mask)

    # Print HSV values if changed
    if (hMin, sMin, vMin, hMax, sMax, vMax) != (phMin, psMin, pvMin, phMax, psMax, pvMax):
        print(f"(hMin = {hMin}, sMin = {sMin}, vMin = {vMin}) | (hMax = {hMax}, sMax = {sMax}, vMax = {vMax})")
        phMin, psMin, pvMin, phMax, psMax, pvMax = hMin, sMin, vMin, hMax, sMax, vMax

    # Display
    cv2.imshow('image', result)
    if cv2.waitKey(10) & 0xFF == ord('q'):
        break

cv2.destroyAllWindows()
