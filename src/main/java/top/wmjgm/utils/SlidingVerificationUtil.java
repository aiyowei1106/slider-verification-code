package top.wmjgm.utils;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.RandomUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * 生成验证码工具
 * @author hanne hll941106@163.com
 * @date 2019-07-02
 **/
@Slf4j
public class SlidingVerificationUtil {

    /**
     * 滑块验证码生成
     * @return
     * @throws IOException
     */
    public static Map<String, Object> createSlidingVerificationImages(String secretKey) throws IOException {
        /**
         * 获取图片  本地获取
         */
        List<String> imagePaths = getPaths();
        Map<String, BufferedImage> imagePathMap = SlidingVerificationUtil.images(imagePaths);
        BufferedImage origetImage = imagePathMap.get("origetImage");
        BufferedImage targetImage = imagePathMap.get("targetImage");

        int cutWidth = targetImage.getHeight()/3;
        int cutLength = targetImage.getHeight()/3;
        /**
         * 随机抠图在原图上的坐标
         */
        int width = targetImage.getWidth();
        int length = targetImage.getHeight();
        Integer randomX = RandomUtils.nextInt(0, width/2/2) + width/2;
        Integer randomY = RandomUtils.nextInt(0, length/2/2) + length/2/2;

        /**
         * 获取抠图图片
         */
        BufferedImage sliderImage = SlidingVerificationUtil.getMarkImage(targetImage, randomX, randomY, cutLength, cutWidth);
        /**
         * 获取抠图区域
         */
        int[][] cutAreaData = SlidingVerificationUtil.getCutAreaData(targetImage.getHeight(), targetImage.getWidth(), cutLength, cutWidth, randomX, randomY);
        /**
         * 滑块 抠图区域上色
         */
        BufferedImage colorProcessingTargetImage = SlidingVerificationUtil.cutByTemplate(targetImage, cutAreaData);

        /**
         * 滑块添加边框
         */
        BufferedImage sliderborderProcessingImage = SlidingVerificationUtil.imagesFrameSlider(sliderImage);
        BufferedImage targetBorderProcessingImage = SlidingVerificationUtil.imagesFrame(colorProcessingTargetImage, randomX, randomY, cutLength, cutWidth);

        /**
         * 高斯模糊
         */
        BufferedImage sliderGaussianBlur = SlidingVerificationUtil.simpleBlur(sliderborderProcessingImage, null);
        BufferedImage targetGaussianBlur = SlidingVerificationUtil.simpleBlur(targetBorderProcessingImage, null);

        /**
         * 压缩图片转jpg
         */
        byte[] oriImageWriter = SlidingVerificationUtil.compressPictures(origetImage);
        byte[] targetImageWriter = SlidingVerificationUtil.compressPictures(targetGaussianBlur);
        byte[] sliderImageWriter = SlidingVerificationUtil.compressPictures(sliderGaussianBlur);

        /**
         * 将byte[]作为输入流
         */
        ByteArrayInputStream oriByteArrayInputStream = new ByteArrayInputStream(oriImageWriter);
        ByteArrayInputStream targetByteArrayInputStream = new ByteArrayInputStream(targetImageWriter);
        ByteArrayInputStream sliderByteArrayInputStream = new ByteArrayInputStream(sliderImageWriter);

        /**
         * 转换类型
         */
        BufferedImage oriBufferedImage = ImageIO.read(oriByteArrayInputStream);
        BufferedImage targetBufferedImage = ImageIO.read(targetByteArrayInputStream);
        BufferedImage sliderBufferedImage = ImageIO.read(sliderByteArrayInputStream);

        /**
         * 图片通过Base64转字符串
         */
        String oriBase64 = SlidingVerificationUtil.imageToBase64(oriBufferedImage);
        String targetBase64 = SlidingVerificationUtil.imageToBase64(targetBufferedImage);
        String sliderBase64 = SlidingVerificationUtil.imageToBase64(sliderBufferedImage);

        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("orii", oriBase64);
        imageMap.put("tari", targetBase64);
        imageMap.put("slii", sliderBase64);
        imageMap.put("cooy", randomY);
        imageMap.put("coox", randomX);

        Map<String, Object> claims = new HashMap<>();
        claims.put("cooy", randomY);
        claims.put("coox", randomX);

        String token = JWTUtil.generateToken(claims, null, secretKey, 1000 * 60 * 2);
        imageMap.put("chet", token);

        /**
         * 输出图片到本地磁盘
         */
//        ImageIO.write(oriBufferedImage, "jpg", new File("E:\\原图.jpg"));
//        ImageIO.write(targetBufferedImage, "jpg", new File("E:\\目标图片.jpg"));
//        ImageIO.write(sliderBufferedImage, "jpg", new File("E:\\滑块图片.jpg"));

        return imageMap;
    }

    /**
     * 读取图片用于做验证码
     * @param imagePaths 图片地址列表
     * @return
     * @throws IOException
     */
    private static Map<String, BufferedImage> images(List<String> imagePaths) throws IOException {
        Map<String, BufferedImage> imageMap = new HashMap<>();
        int randomIndex = RandomUtils.nextInt(0, imagePaths.size());
        String imagePath = imagePaths.get(randomIndex);
        BufferedImage origetImage = Thumbnails.of(ImageIO.read(new FileInputStream(imagePath)))
                .size(350, 213)
                .asBufferedImage();
        BufferedImage targetImage = Thumbnails.of(ImageIO.read(new FileInputStream(imagePath)))
                .size(350, 213)
                .asBufferedImage();

        imageMap.put("origetImage", origetImage);
        imageMap.put("targetImage", targetImage);
        return imageMap;
    }

    /**
     * 抠图方块裁剪,得到滑动方块并返回
     * @param targetImage 目标图片
     * @param x x轴
     * @param y y轴
     * @param cutLength 图片长度
     * @param cutWidth 图片宽度
     * @return
     */
    private static BufferedImage getMarkImage(BufferedImage targetImage, int x, int y, int cutLength, int cutWidth) throws IOException {
        InputStream inputStream = null;
        ImageInputStream imageInputStream = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(targetImage, "jpg", byteArrayOutputStream);
            inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName("jpg");
            ImageReader imageReader = iterator.next();
            /**
             * 获取图片流
             */
            imageInputStream = ImageIO.createImageInputStream(inputStream);
            imageReader.setInput(imageInputStream, true);
            ImageReadParam imageReadParam = imageReader.getDefaultReadParam();
            Rectangle rectangle = new Rectangle(x, y, cutLength, cutWidth);
            /**
             * 提供一个BufferedImage,用作解码像素数据目标
             */
            imageReadParam.setSourceRegion(rectangle);
            BufferedImage bufferedImage = imageReader.read(0, imageReadParam);
            return bufferedImage;
        }finally {
            if (inputStream != null){
                inputStream.close();
            }
            if (imageInputStream != null){
                imageInputStream.close();
            }
        }
    }

    /**
     * 获取抠图区域坐标,通过抠图坐标和长宽确定抠图区域的坐标,生成滑块形状
     *      0 透明像素
     *      1 滑块像素
     * @param targetLength 原图长度
     * @param targetWidth 原图宽度
     * @param cutLength 抠图长度
     * @param cutWidth 抠图宽度
     * @param cutX 裁剪区域X坐标
     * @param cutY 裁剪区域Y坐标
     * @return
     */
    private static int[][] getCutAreaData(int targetLength, int targetWidth, int cutLength, int cutWidth, int cutX, int cutY) {
        int[][] cutAreaData = new int[targetLength][targetWidth];
        for (int i = 0; i < targetLength; i++){
            for (int j = 0; j < targetWidth; j++){
                if (i < cutY + cutLength && i >= cutY && j < cutX + cutWidth && j >= cutX){
                    cutAreaData[i][j] = 1;
                }else {
                    cutAreaData[i][j] = 0;
                }
            }
        }
        return cutAreaData;
    }

    /**
     * 对1(滑块)的地方进行上色
     * @param targetImage 目标图片
     * @param templateImage 抠图区域坐标
     * @return
     */
    private static BufferedImage cutByTemplate(BufferedImage targetImage, int[][] templateImage){
        for (int i = 0; i < targetImage.getWidth(); i++) {
            for (int j = 0; j < targetImage.getHeight(); j++) {
                int rgb = templateImage[j][i];
                /**
                 * 原图中对应位置变色处理
                 */
                int rgb_ori = targetImage.getRGB(i,  j);
                if (rgb == 1) {
                    int b  = (rgb_ori & 0xff0000) >> 16;
                    int g = (rgb_ori & 0xff00) >> 8;
                    int r = (rgb_ori & 0xff);
                    int color = (int)(b* 0.3 + g * 0.59 + r * 0.11);
                    color = color > 128 ? 255 : color;
                    targetImage.setRGB(i, j, (color << 16) | (color << 8) | color);
                }
            }
        }
        return targetImage;
    }

    /**
     * 高斯模糊图片
     * @param radius
     * @param horizontal
     * @return
     */
    private static ConvolveOp getGaussianBlurFilter(int radius, boolean horizontal) {
        if (radius < 1) {
            throw new IllegalArgumentException("Radius must be >= 1");
        }
        int size = radius * 2 + 1;
        float[] data = new float[size];
        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;
        for (int i = -radius; i <= radius; i++) {
            float distance = i * i;
            int index = i + radius;
            data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += data[index];
        }
        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }
        Kernel kernel;
        if (horizontal) {
            kernel = new Kernel(size, 1, data);
        } else {
            kernel = new Kernel(1, size, data);
        }
        return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    }
    private static BufferedImage simpleBlur(BufferedImage source, BufferedImage dest) {
        BufferedImageOp op = getGaussianBlurFilter(2, false);
        return op.filter(source, dest);
    }

    /**
     * 滑块图片添加边框
     * @param targetImage 目标图片
     * @param x 原点X坐标
     * @param y 原点Y坐标
     * @param cutLength 滑块长度
     * @param cutWidth 滑块宽度
     * @return
     */
    private static BufferedImage imagesFrame(BufferedImage targetImage, int x, int y, int cutLength, int cutWidth){
        Graphics graphics = targetImage.getGraphics();
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.drawRect(x, y, cutWidth, cutLength);
        return targetImage;
    }
    private static BufferedImage imagesFrameSlider(BufferedImage sliderImage){
        int height = sliderImage.getHeight();
        int width = sliderImage.getWidth();
        Graphics g = sliderImage.getGraphics();
        Color c1 = new Color(173,216,230);
        /**
         * 画笔颜色
         */
        g.setColor(Color.YELLOW);
        /**
         * 矩形框(原点x坐标，原点y坐标，矩形的长，矩形的宽)
         */
        g.drawRect(0, 0, width-1, height-1);
        return sliderImage;
    }

    /**
     * 图片压缩处理
     * @param targetImage 目标图片
     * @return
     * @throws IOException
     */
    private static byte[] compressPictures(BufferedImage targetImage) throws IOException {
        /**
         * 得到指定Format图片的writer
         */
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = iter.next();
        /**
         * 获取指定writer的输出参数设置(ImageWriteParam )
         */
        ImageWriteParam imageWriteParam = writer.getDefaultWriteParam();
        /**
         * 设置可否压缩
         */
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        /**
         * 设置压缩质量参数
         */
        imageWriteParam.setCompressionQuality(1f);
        imageWriteParam.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
        ColorModel colorModel = ColorModel.getRGBdefault();
        /**
         * 指定压缩时使用的色彩模式
         */
        imageWriteParam.setDestinationType(new ImageTypeSpecifier(colorModel,colorModel.createCompatibleSampleModel(16, 16)));
        /**
         * 取得内存输出流
         */
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        writer.setOutput(ImageIO.createImageOutputStream(byteArrayOutputStream));
        IIOImage iIamge = new IIOImage(targetImage, null, null);
        writer.write(null, iIamge, imageWriteParam);
        return  byteArrayOutputStream.toByteArray();
    }

    /**
     * base64图片/字符串互转
     * @param targetImage 目标图片
     * @return
     * @throws IOException
     */
    private static String imageToBase64(BufferedImage targetImage) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(targetImage, "jpg", byteArrayOutputStream);
        byte[] imagedata = byteArrayOutputStream.toByteArray();
        BASE64Encoder base64Encoder = new BASE64Encoder();
        String base64Image = base64Encoder.encodeBuffer(imagedata).trim();
        /**
         * 删除\n\r
         */
        base64Image = base64Image.replaceAll("\\n", "").replaceAll("\\r", "");
        return base64Image;
    }
    private static BufferedImage stringToBase64(String base64String) {
        try {
            BASE64Decoder decoder=new BASE64Decoder();
            byte[] bytes1 = decoder.decodeBuffer(base64String);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes1);
            return ImageIO.read(bais);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取/resources/image下图片路径
     * @return
     * @throws IOException
     */
    private static List<String> getPaths() throws IOException {

        List<String> paths = new ArrayList<>();
        File file = new File("");
        String filePath = file.getCanonicalPath();
        file = new File(filePath+"/src/main/resources/image");
        if(file.isDirectory()){
            File []files = file.listFiles();
            for(File fileIndex:files){
                if(fileIndex.isDirectory()){
                    getPaths();
                }else {
                    paths.add(fileIndex.getPath());
                }
            }
        }
        return paths;
    }
}
