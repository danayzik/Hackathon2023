import cv2
import numpy as np
import time
# Create a VideoCapture object and read from input file
# If the input is the camera, pass 0 instead of the video file name
cap = cv2.VideoCapture('b.mp4')
 
# Check if camera opened successfully
if (cap.isOpened()== False): 
  print("Error opening video stream or file")

def region_selection(image):
    """
    Determine and cut the region of interest in the input image.
    Parameters:
        image: we pass here the output from canny where we have
        identified edges in the frame
    """
    # create an array of the same size as of the input image
    mask = np.zeros_like(image)  
    # if you pass an image with more then one channel
    if len(image.shape) > 2:
        channel_count = image.shape[2]
        ignore_mask_color = (255,) * channel_count
    # our image only has one channel so it will go under "else"
    else:
          # color of the mask polygon (white)
        ignore_mask_color = 255
    # creating a polygon to focus only on the road in the picture
    # we have created this polygon in accordance to how the camera was placed
    rows, cols = image.shape[:2]
    bottom_left  = [cols * 0.70, rows * 0.8]
    top_left     = [cols * 0.45, rows * 0.6]
    bottom_right = [cols * 0.9,  rows * 0.8]
    top_right    = [cols * 0.65,  rows * 0.6]
    vertices = np.array([[bottom_left, top_left, top_right, bottom_right]], dtype=np.int32)
    # filling the polygon with white color and generating the final mask
    cv2.fillPoly(mask, vertices, ignore_mask_color)
    # performing Bitwise AND on the input image and mask to get only the edges on the road
    masked_image = cv2.bitwise_and(image, mask)
    return masked_image
# Read until video is completed
while(cap.isOpened()):
  # Capture frame-by-frame
  ret, rframe = cap.read()
  #rframe = cv2.imread('a2.jpg')
  frame=np.copy(rframe)
  frame = cv2.GaussianBlur(frame, (5, 5), 0)
  height,width,_ = frame.shape
  frame = region_selection(frame)
  hsv=cv2.cvtColor(frame,cv2.COLOR_BGR2HSV)
  blue_mask = cv2.inRange(hsv, np.array([70, 70, 70]), np.array([130, 130, 130]))
  blue_pixels = cv2.bitwise_and(frame, frame, mask=blue_mask)
  if ret == True:
    # Display the resulting frame
    gray=cv2.cvtColor(blue_pixels,cv2.COLOR_BGR2GRAY) 
    can = cv2.Canny(gray,0,100)
    # Filter contours by size
    contours, hierarchy = cv2.findContours(can, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)
    filtered_contours = []
    data=[]
    for contour in contours:
        length = cv2.arcLength(contour, True)
        area = cv2.contourArea(contour)
        if area> 50 and length>50:  # Set your desired minimum contour area here
            filtered_contours.append(contour)
            data.append((area,length))
    #print(data)
    cv2.drawContours(rframe, filtered_contours, -1, (0, 255, 0), 2)
    cv2.imshow('blue_pixels',rframe)

    # Press Q on keyboard to  exit
    if cv2.waitKey(25) & 0xFF == ord('q'):
      a=rframe
      cv2.imwrite('l.png',a)
      print(data)
      break
  # Break the loop
  else: 
    break
  #time.sleep(0.1)
 
# When everything done, release the video capture object
cap.release()
 
# Closes all the frames
#cv2.destroyAllWindows()
