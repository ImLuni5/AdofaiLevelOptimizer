import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class Main {
    static final String[] EXTENSIONS = new String[]{
            "jpg", "png", "jpeg" // and other formats you need
    };
    static final FilenameFilter IMAGE_FILTER = (dir, name) -> {
        for (final String ext : EXTENSIONS) {
            if (name.endsWith("." + ext)) {
                return (true);
            }
        }
        return (false);
    };

    private static void resizeImage(File file, int width, int height) {
        try {
            BufferedImage image = ImageIO.read(file);
            Image originalImage = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);

            int type = ((image.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : image.getType());
            BufferedImage resizedImage = new BufferedImage(width, height, type);

            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, width, height, null);
            g2d.dispose();
            g2d.setComposite(AlphaComposite.Src);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ImageIO.write(resizedImage, file.getName().split("\\.")[1], file);
        } catch (IOException e) {

        }
    }

    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("맵 파일 경로: ");
        File dir = new File(br.readLine());
        System.out.print("가로 픽셀: ");
        int width = Integer.parseInt(br.readLine());
        System.out.print("세로 픽셀: ");
        final int height = Integer.parseInt(br.readLine());
        System.out.print("허용 오차범위: ");
        int over = Integer.parseInt(br.readLine());

        int removedLength = 0, count = 0;

        if (dir.isDirectory()) { // make sure it's a directory
            for (final File f : dir.listFiles(IMAGE_FILTER)) {
                BufferedImage img;
                try {
                    img = ImageIO.read(f);
                    if (img.getWidth() > width + over || img.getHeight() > height + over) {
                        int currentWidth = img.getWidth(), currentHeight = img.getHeight();

                        System.out.println("해상도가 큰 이미지 발견: " + f.getName());
                        System.out.println(" 가로 : " + currentWidth);
                        System.out.println(" 세로: " + currentHeight);
                        System.out.println(" 크기  : " + f.length());
                        System.out.println();

                        long oldLength = f.length();

                        double userWidth = (double) width / (double) height;
                        double imageWidth = (double) currentWidth / (double) currentHeight;
                        int setWidth, setHeight;
                        if (userWidth <= imageWidth) {
                            setWidth = width;
                            setHeight = (int) ((float) currentHeight / (float) currentWidth * (float) width);
                        } else {
                            setHeight = height;
                            setWidth = (int) ((float) currentWidth / (float) currentHeight * (float) height);
                        }
                        resizeImage(f, setWidth, setHeight);

                        img = ImageIO.read(f);
                        System.out.println("조정됨: " + f.getName());
                        System.out.println(" 가로 : " + img.getWidth());
                        System.out.println(" 세로: " + img.getHeight());
                        System.out.println(" 크기  : " + f.length());
                        System.out.println();

                        removedLength += oldLength - f.length();
                        count++;
                    }
                } catch (final IOException e) {

                }
            }
            System.out.println(count + "개의 이미지의 해상도가 조정되었고, " + ((double) removedLength / 1048576f) + "MB만큼 아꼈습니다.");
        }
    }
}
