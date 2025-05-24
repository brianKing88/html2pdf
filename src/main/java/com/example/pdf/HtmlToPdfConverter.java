package com.example.pdf;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.io.font.PdfEncodings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlToPdfConverter {

    private static final Logger LOGGER = Logger.getLogger(HtmlToPdfConverter.class.getName());
    public static final float POINTS_PER_MM = 72f / 25.4f; // Constant for mm to points conversion
    
    // 默认位移参数（负值向左上移动）
    // private static final float DEFAULT_X_OFFSET = -10f;
    // private static final float DEFAULT_Y_OFFSET = -10f;
    private static final float DEFAULT_X_OFFSET = 0;
    private static final float DEFAULT_Y_OFFSET = 0;

    /**
     * Creates a PageSize object from width and height in millimeters.
     *
     * @param widthInMm  Width in millimeters.
     * @param heightInMm Height in millimeters.
     * @return PageSize object.
     */
    public static PageSize createPageSizeInMillimeters(float widthInMm, float heightInMm) {
        float widthInPoints = widthInMm * POINTS_PER_MM;
        float heightInPoints = heightInMm * POINTS_PER_MM;
        return new PageSize(widthInPoints, heightInPoints);
    }

    /**
     * html转pdf
     *
     * @param file         文件
     * @param customFontDir 字体目录
     * @param outputStream 输出流
     * @param pageSize     页面大小
     */
    public static void convertToPdf(File file, String customFontDir, OutputStream outputStream, PageSize pageSize) throws IOException {
        File pdfFile = null;
        InputStream htmlInputStream = null;
        InputStream processedHtmlStream = null;
        PdfReader pdfReader = null;

        try {
            htmlInputStream = new FileInputStream(file);
            String pdfFileName = Objects.requireNonNull(file.getName()).substring(0, file.getName().lastIndexOf("."));
            
            String parentDirString = file.getParent();
            if (parentDirString == null) {
                parentDirString = System.getProperty("java.io.tmpdir");
                if (parentDirString == null) { 
                    parentDirString = "."; 
                }
            }
            pdfFile = Paths.get(parentDirString, pdfFileName + ".pdf").toFile();

            try (PdfWriter pdfWriter = new PdfWriter(pdfFile)) {
                PdfDocument pdfDocument = new PdfDocument(pdfWriter);

                if (pageSize != null) {
                    pdfDocument.setDefaultPageSize(pageSize);
                } else {
                    pdfDocument.setDefaultPageSize(PageSize.A4);
                }

                ConverterProperties properties = new ConverterProperties();
                FontProvider fontProvider = new FontProvider();
                boolean customFontsProcessed = false;

                if (customFontDir != null && !customFontDir.trim().isEmpty()) {
                    File fontDirFile = new File(customFontDir);
                    if (fontDirFile.exists() && fontDirFile.isDirectory()) {
                        LOGGER.info("Attempting to load specific Microsoft YaHei fonts from: " + fontDirFile.getAbsolutePath());

                        String yaheiRegularPath = Paths.get(fontDirFile.getAbsolutePath(), "msyh.ttf").toString();
                        String yaheiBoldPath = Paths.get(fontDirFile.getAbsolutePath(), "msyhbd.ttf").toString();
                        // Optional: for YaHei Light if available and needed for font-weight: lighter or specific values
                        // String yaheiLightPath = Paths.get(fontDirFile.getAbsolutePath(), "msyhl.ttf").toString(); 

                        File yaheiRegularFile = new File(yaheiRegularPath);
                        File yaheiBoldFile = new File(yaheiBoldPath);
                        // File yaheiLightFile = new File(yaheiLightPath);

                        if (yaheiRegularFile.exists() && yaheiBoldFile.exists()) {
                            fontProvider.addFont(yaheiRegularPath);
                            fontProvider.addFont(yaheiBoldPath);
                            
                            LOGGER.info("Successfully loaded: " + yaheiRegularPath);
                            LOGGER.info("Successfully loaded: " + yaheiBoldPath);
                            // if (yaheiLightFile.exists()) { LOGGER.info("Successfully loaded: " + yaheiLightPath); }
                            LOGGER.info("FontProvider configured to prioritize Microsoft YaHei. Unloaded fonts like 'SimSun' or 'SimHei' should fallback to YaHei if CSS specifies them or if YaHei is a generic fallback.");
                            customFontsProcessed = true;
                        } else {
                            String notFoundMessage = "Required Microsoft YaHei fonts not found in " + fontDirFile.getAbsolutePath() + ":";
                            if (!yaheiRegularFile.exists()) notFoundMessage += " msyh.ttf missing;";
                            if (!yaheiBoldFile.exists()) notFoundMessage += " msyhbd.ttf missing;";
                            LOGGER.warning(notFoundMessage);
                            customFontsProcessed = false; // Fallback if files are missing
                        }
                    } else {
                        LOGGER.warning("Custom font directory not found or not a directory: " + customFontDir);
                        customFontsProcessed = false; // Fallback if dir is invalid
                    }
                }

                if (!customFontsProcessed) {
                    LOGGER.info("Custom YaHei font processing failed or not configured. Using fallback font loading (STSongStd-Light or standard PDF fonts).");
                    try {
                        PdfFont sysFont = PdfFontFactory.createFont("STSongStd-Light", PdfEncodings.IDENTITY_H);
                        fontProvider.addFont(sysFont.getFontProgram(), PdfEncodings.IDENTITY_H);
                        LOGGER.info("Fallback font STSongStd-Light loaded.");
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Fallback STSongStd-Light font not found. Using iText standard PDF fonts.", e);
                        fontProvider.addStandardPdfFonts(); // Last resort
                    }
                }
 
                properties.setFontProvider(fontProvider);

                processedHtmlStream = readInputStream(htmlInputStream); 
                htmlInputStream = null; 

                if (processedHtmlStream == null) {
                    throw new IOException("Failed to read and process input HTML stream.");
                }
                
                // 重要：在创建Document前，首先在左上角生成一个位移标记元素
                // 这样内容会自动相对于这个标记进行定位
                PdfCanvas shiftCanvas = new PdfCanvas(pdfDocument.addNewPage());
                shiftCanvas.concatMatrix(1, 0, 0, 1, DEFAULT_X_OFFSET, DEFAULT_Y_OFFSET);
                
                // Remove margins by using a Document with 0 margins and convert HTML to Document.
                com.itextpdf.layout.Document document = HtmlConverter.convertToDocument(processedHtmlStream, pdfDocument, properties);
                document.setMargins(0f, 0f, 0f, 0f);
                
                // 关闭文档
                document.close();
                pdfDocument.close();
            } 

            pdfReader = new PdfReader(pdfFile);
            try (PdfWriter writerToOutputStream = new PdfWriter(outputStream);
                 PdfDocument finalPdfDoc = new PdfDocument(pdfReader, writerToOutputStream)) {
                pdfReader = null; 

                // int numberOfPages = finalPdfDoc.getNumberOfPages();
                // if (numberOfPages > 0) {
                //     addPageFoot(finalPdfDoc, numberOfPages);
                // }
            }

        } finally {
            if (htmlInputStream != null) {
                try {
                    htmlInputStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error closing original HTML input stream: " + e.getMessage(), e);
                }
            }
            if (processedHtmlStream != null) {
                try {
                    processedHtmlStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error closing processed HTML input stream: " + e.getMessage(), e);
                }
            }
            if (pdfReader != null) { 
                 try {
                    pdfReader.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error closing PdfReader: " + e.getMessage(), e);
                }
            }
            if (pdfFile != null && pdfFile.exists()) {
                if (!pdfFile.delete()) {
                    LOGGER.log(Level.WARNING, "Could not delete temporary PDF file: " + pdfFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 添加页码和总页码
     */
    static void addPageFoot(PdfDocument pdfDoc, Integer numberOfPages) {
        PdfFont pdfFont = null;
        try {
            pdfFont = PdfFontFactory.createFont("STSongStd-Light", PdfEncodings.IDENTITY_H);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "STSongStd-Light font for page numbers not found, using default font.", e);
            try {
                pdfFont = PdfFontFactory.createFont();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Default font for page numbers could not be created.", ex);
                return;
            }
        }

        for (int i = 1; i <= numberOfPages; i++) {
            PdfPage page = pdfDoc.getPage(i);
            Rectangle pageSize = page.getPageSize();
            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
            try (Canvas canvas = new Canvas(pdfCanvas, pageSize)) {
                float x = (pageSize.getLeft() + pageSize.getRight()) / 2;
                float y = pageSize.getBottom() + 15;
                Paragraph paragraph = new Paragraph("第" + pdfDoc.getPageNumber(page) + "页/共" + numberOfPages + "页")
                        .setFontSize(10)
                        .setFont(pdfFont);
                canvas.showTextAligned(paragraph, x, y, TextAlignment.CENTER);
            } 
        }
    }

    /**
     * 读取HTML 流文件，并查询当中的&nbsp;或类似符号直接替换为空格
     *
     * @param inputStream 输入流
     * @return 处理后的输入流
     */
    private static InputStream readInputStream(InputStream inputStreamToProcess) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStreamToProcess.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            String content = baos.toString(StandardCharsets.UTF_8.name());
            Pattern compile = Pattern.compile("\\&[a-zA-Z]{1,10};", Pattern.CASE_INSENSITIVE);
            Matcher matcher = compile.matcher(content);
            String replaceAll = matcher.replaceAll(" ");
            return new ByteArrayInputStream(replaceAll.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error reading or processing input stream: " + e.getMessage(), e);
            return null;
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error closing ByteArrayOutputStream: " + e.getMessage(), e);
            }
            if (inputStreamToProcess != null) {
                try {
                    inputStreamToProcess.close(); 
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error closing input stream in readInputStream: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 将一个字符串转化为输入流
     *
     * @param sInputString 字符串
     * @return 输入流
     */
    public static InputStream getStringStream(String sInputString) {
        if (sInputString != null && !sInputString.trim().isEmpty()) {
            try {
                return new ByteArrayInputStream(sInputString.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error converting string to stream: " + e.getMessage(), e);
            }
        }
        return null;
    }
} 