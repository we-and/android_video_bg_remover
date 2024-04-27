import cv2
import numpy as np

def remove_background(input_path, output_path):
    # Open the video
    cap = cv2.VideoCapture(input_path)

    # Define the codec and create a VideoWriter object to save the output
    fourcc = cv2.VideoWriter_fourcc(*'mp4v') # or 'XVID'
    out = cv2.VideoWriter(output_path, fourcc, 20.0, (int(cap.get(cv2.CAP_PROP_FRAME_WIDTH)), int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))))

    # Define the range of green color in HSV
    lower_green = np.array([36, 25, 25])
    upper_green = np.array([86, 255,255])

    while(cap.isOpened()):
        ret, frame = cap.read()
        if ret:
            # Convert the image from BGR to HSV color space
            hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)

            # Create a mask for green color
            mask = cv2.inRange(hsv, lower_green, upper_green)

            # Invert the mask to get parts that are not green
            mask_inv = cv2.bitwise_not(mask)

            # Create a black image of the same dimensions as frame
            black_background = np.zeros_like(frame)

            # Take only the non-green parts from the original video frame
            frame_no_green = cv2.bitwise_and(frame, frame, mask=mask_inv)

            # Combine the result with the black background
            result = cv2.bitwise_or(black_background, frame_no_green)

            # Write the frame into the file 'output.mp4'
            out.write(result)

            # Optionally, display the result live
            # cv2.imshow('frame', result)
            # if cv2.waitKey(1) & 0xFF == ord('q'):
            #     break
        else:
            break

    # Release everything if job is finished
    cap.release()
    out.release()
    cv2.destroyAllWindows()