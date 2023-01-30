import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Main {
    static final String[] EXTENSIONS = new String[]{
            "jpg", "png", "jpeg" // and other formats you need
    };
    static final FilenameFilter IMAGE_FILTER = (dir, name) -> {
        for (final String ext : EXTENSIONS) {
            if (name.toLowerCase().endsWith("." + ext)) {
                return (true);
            }
        }
        return (false);
    };

    static final HashMap<String, Float> widthRatio = new HashMap<>();
    static final HashMap<String, Float> heightRatio = new HashMap<>();
    static final HashMap<String, String> tag = new HashMap<>();

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
            e.printStackTrace();
        }
    }

    private static void replaceSize(File level) throws IOException {
        StringBuilder decorationLevel = new StringBuilder();
        StringBuilder mainLevel = new StringBuilder();
        try {
            int nBuffer;

            BufferedReader br = new BufferedReader(new FileReader(level));
            while ((nBuffer = br.read()) != -1) {
                decorationLevel.append((char) nBuffer);
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] level1 = decorationLevel.toString().split("\"decorations\":");
        String[] decorations = level1[1].split("},");
        String[] level2 = level1[0].split("},");
        decorationLevel.setLength(0);
        boolean isChecked = false;
        for (String decoration : decorations) {
            for (Map.Entry<String, Float> image : widthRatio.entrySet()) {
                if (decoration.contains('"' + image.getKey() + '"')) {
                    String[] scale = decoration.split("\"scale\": \\[");
                    String[] scale2 = scale[1].split("], \"tile");
                    String[] ratio = scale2[0].split(", ");

                    float width = Float.parseFloat(ratio[0]) * image.getValue();
                    float height = Float.parseFloat(ratio[1]) * heightRatio.get(image.getKey());

                    decorationLevel.append(scale[0]).append("\"scale\": [").append(width).append(", ").append(height).append("], \"tile").append(scale2[1]).append("},");

                    tag.put(image.getKey(), scale2[1].split("\"tag\": \"")[1].split("\",")[0]);
                    isChecked = true;
                    break;
                }
            }
            if (!isChecked) decorationLevel.append(decoration).append("},");
            else isChecked = false;
        }
        for (String decorationMove : level2) {
            for (Map.Entry<String, Float> image : widthRatio.entrySet()) {
                if (decorationMove.contains(tag.getOrDefault(image.getKey(), "erfkjsdlfkj이히이히이건없겠지??")) && decorationMove.contains("\"MoveDecorations\"") && decorationMove.contains("\"scale\"")) {
                    String[] scaleMove = decorationMove.split("\"scale\": \\[");
                    String[] scaleMove2 = scaleMove[1].split("],");
                    String[] ratioMove = scaleMove2[0].split(", ");

                    float widthMove = Float.parseFloat(ratioMove[0]) * image.getValue();
                    float heightMove = Float.parseFloat(ratioMove[1]) * heightRatio.get(image.getKey());

                    mainLevel.append(scaleMove[0]).append("\"scale\": [").append(widthMove).append(", ").append(heightMove);
                    for (int i = 1; i < scaleMove2.length; i++) mainLevel.append("],").append(scaleMove2[i]);
                    mainLevel.append("},");

                    isChecked = true;
                    break;
                }
            }
            if (!isChecked) mainLevel.append(decorationMove).append("},");
            else isChecked = false;
        }
        decorationLevel.setLength(decorationLevel.length() - 2);
        mainLevel.setLength(mainLevel.length() - 2);
        mainLevel.append("\"decorations\":").append(decorationLevel);
        BufferedWriter bw = new BufferedWriter(new FileWriter(level));
        bw.write(mainLevel.toString(), 0, mainLevel.length());
        bw.flush();
        bw.close();
    }

    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("맵 파일 경로: ");
        File level = new File(br.readLine());
        File dir = new File(level.getPath().substring(0, level.getPath().lastIndexOf('\\')));
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
                        widthRatio.put(f.getName(), (float) currentWidth / (float) setWidth);
                        heightRatio.put(f.getName(), (float) currentHeight / (float) setHeight);

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
                    e.printStackTrace();
                }
            }
            replaceSize(level);
            System.out.println(count + "개의 이미지의 해상도가 조정되었고, " + ((double) removedLength / 1048576f) + "MB만큼 아꼈습니다.");
        }
    }
}
