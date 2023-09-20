
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;
	BufferedImage originalImage;
	int width = 7680; // default image width and height // adjusted for 16k images
	int height = 4320;

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		try
		{
			int frameLength = width*height*3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					byte r = bytes[ind];
					byte g = bytes[ind+height*width];
					byte b = bytes[ind+height*width*2];

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,y,pix);
					ind++;
				}
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void showIms(String[] args){

		// ensuring that there are 4 arguments:
		if (args.length < 4){
			System.out.println("Error! provide these arguments: <image url <scale> <anti-aliasing 0 or 1> <window size>");
			return;
		}

		// read parameters from command line
		float scale = Float.parseFloat(args[1]);
		boolean isAntiAlias = Integer.parseInt(args[2]) == 1;
		int windowSize = Integer.parseInt(args[3]);

		// parameter(s) Check
		if (scale > 1.0) {
			System.out.println("Error! please input a scale between 0 to 1 (inclusive)");
			return;
		} else if (windowSize <= 0) {
			System.out.println("Error! please input a positive window size");
			return;
		}

		System.out.println("Image url: " + args[0]);
		System.out.println("Scale: " + scale);
		System.out.println("Anti-Aliasing: " + isAntiAlias);
		System.out.println("Window Size: " + windowSize);

		// read in the specified image
		imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], imgOne);

		originalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, args[0], originalImage);

		// Low Pass Filter checker
		if(isAntiAlias && scale < 1.0){
			imgOne = lowPassFilter(imgOne);
		}

		// SCALING: check if scaling is required
		if (scale != 1.0 && scale > 0) {
			imgOne = scaleImage(imgOne, scale);
		}


		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		lbIm1 = new JLabel(new ImageIcon(imgOne));

		// zoomRegion
		lbIm1.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (e.isControlDown()) {

					// determining position on where to start capturing the zoom area,
					// based on the cursor's position being in the center
					int halfZoomRegionWidth = windowSize / 2;
					int halfZoomRegionHeight = windowSize / 2;

					int cursorX = e.getX();
					int cursorY = e.getY();

					// since cursorX/cursorY are coordinates on the scaled-down image
					// we need to map them back to the originalImage's coordinates
					// conversion ensures accurate mapping between the displayed image and the original one
					int originalCursorX = Math.round(cursorX / scale);
					int originalCursorY = Math.round(cursorY / scale);

					// shifting by the half-width/height values, the zoom is centered on the cursor.
					int x = originalCursorX - halfZoomRegionWidth;
					int y = originalCursorY - halfZoomRegionHeight;

					// boundary checks
					x = Math.max(0, Math.min(x, originalImage.getWidth() - windowSize));
					y = Math.max(0, Math.min(y, originalImage.getHeight() - windowSize));

					BufferedImage zoomRegion = originalImage.getSubimage(x, y, windowSize, windowSize);

					// to provide a zoom effect, we need to enlarge the region of interest
					BufferedImage scaledZoom = new BufferedImage(windowSize, windowSize, BufferedImage.TYPE_INT_RGB);
					Graphics2D graphics = scaledZoom.createGraphics();
					graphics.drawImage(zoomRegion, 0, 0, windowSize, windowSize, null);
					graphics.dispose();

					// to superimpose the magnified region over the original image
					BufferedImage overlay = new BufferedImage(imgOne.getWidth(), imgOne.getHeight(), BufferedImage.TYPE_INT_RGB);
					Graphics g = overlay.createGraphics();
					g.drawImage(imgOne, 0, 0, null);

					int overlayX = cursorX - windowSize / 2;
					int overlayY = cursorY - windowSize / 2;

					// boundary checks
					overlayX = Math.max(0, Math.min(overlayX, imgOne.getWidth() - windowSize));
					overlayY = Math.max(0, Math.min(overlayY, imgOne.getHeight() - windowSize));

					if (!isAntiAlias) {
						scaledZoom = lowPassFilter(scaledZoom);
					}

					g.drawImage(scaledZoom, overlayX, overlayY, null);
					g.dispose();

					lbIm1.setIcon(new ImageIcon(overlay));
				}
			}
		});


		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public BufferedImage lowPassFilter(BufferedImage imgOne){
		float[] matrix = {
				1/25f, 1/25f, 1/25f, 1/25f, 1/25f,
				1/25f, 1/25f, 1/25f, 1/25f, 1/25f,
				1/25f, 1/25f, 1/25f, 1/25f, 1/25f,
				1/25f, 1/25f, 1/25f, 1/25f, 1/25f,
				1/25f, 1/25f, 1/25f, 1/25f, 1/25f,
		};

		BufferedImageOp convolveOp = new ConvolveOp(new Kernel(5,5, matrix), ConvolveOp.EDGE_NO_OP, null);
		return convolveOp.filter(imgOne, null);
	}

	private BufferedImage scaleImage(BufferedImage originalImage, double scale) {
		int new_w = (int) (originalImage.getWidth() * scale);
		int new_h = (int) (originalImage.getHeight() * scale);

		BufferedImage scaledImage = new BufferedImage(new_w, new_h, originalImage.getType());
		Graphics2D graphics = scaledImage.createGraphics();

		// improving quality of the scaled image
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		graphics.drawImage(originalImage, 0, 0, new_w, new_h, null);
		graphics.dispose();
		return scaledImage;
	}


	public static void main(String[] args) {
		ImageDisplay ren = new ImageDisplay();
		ren.showIms(args);
	}

}
