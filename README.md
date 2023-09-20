# image-resampling-and-filtering

Compile: `javac ImageDisplay.java`
Run: `java ImageDisplay [FILE_PATH] [S] [A] [w]`

## Description
Created an application that will help view the image in a resized format and optionally browse through all the details in `Java`

## Input to your program will take four parameters:
- The first parameter is the name of the image, which will be provided in an 8 bit per channel RGB format (Total 24 bits per pixel). Assume all images in 16xHD format 7680x4320.
- The second parameter is a floating-point value S (between 0 and 1) suggesting by how much the image has to be down scaled so as to fit in a window for display. This will result in resampling the image.
- The third parameter will be a Boolean value A (0 or 1) suggesting whether you want to deal with aliasing. A 0 signifies do nothing (aliasing will remain in your output) and a 1 signifies that anti-aliasing should be performed by using an averaging low pass filter.
- The fourth parameter will give the square window width/height w for showing the original image overlaid if the control key is pressed. As you move your mouse around the image with your control key pressed, you should see the original detailed image overlaid about the mouse area in a w x w window.
