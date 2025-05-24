package com.example.pdf;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assume;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.*;

public class HtmlToPdfConverterTest {

    private File tempInputHtmlFileForSimpleTest;
    private File tempOutputPdfFileForSimpleTest;
    private File tempOutputPdfFileForProvidedTest;
    private File tempInputHtmlFileForExtremeTest;
    private File tempOutputPdfFileForExtremeTest;
    private File tempOutputPdfFileForApiTest;
    private final String FONT_DIRECTORY = "src/fonts"; // Define font directory path

    @Before
    public void setUp() {
        // Potentially create a dedicated test output directory if it doesn't exist
        File outputDir = new File("test_outputs");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }

    @Test
    public void testConvertSimpleHtmlToPdf() throws IOException {
        String simpleHtmlContent = 
        "<!DOCTYPE html>\n" +
        "<html>\n" +
        "<head>\n" +
        "    <title>Test</title>\n" +
        "    <meta charset='UTF-8'>\n" +
        "    <style>\n" +
        "        html, body { width: 100%; height: 100%; margin: 0; padding: 0; box-sizing: border-box; border: 1px solid red; }\n" +
        "        body { font-family: 'Microsoft YaHei', sans-serif; } /* Default to YaHei for page, added red border & 100% dimensions */\n" +
        "        .yahei { font-family: 'Microsoft YaHei', 微软雅黑; }\n" +
        "        .simsun { font-family: SimSun, 宋体; } /* Test if this maps to YaHei */\n" +
        "        .simhei { font-family: SimHei, 黑体; } /* Test if this maps to YaHei */\n" +
        "        .bold { font-weight: bold; }\n" +
        "    </style>\n" +
        "</head>\n" +
        "<body>\n" +
        "    <h1>Font Test Document</h1>\n" +
        "    <p class='yahei'>This paragraph uses Microsoft YaHei (微软雅黑) explicitly.</p>\n" +
        "    <p class='yahei bold'>This paragraph uses Microsoft YaHei Bold (微软雅黑粗体) explicitly.</p>\n" +
        "    <hr>\n" +
        "    <p class='simsun'>This paragraph requests SimSun (宋体). It should render using Microsoft YaHei.</p>\n" +
        "    <p class='simsun bold'>This paragraph requests SimSun Bold (宋体粗体). It should render using Microsoft YaHei Bold.</p>\n" +
        "    <hr>\n" +
        "    <p class='simhei'>This paragraph requests SimHei (黑体). It should render using Microsoft YaHei.</p>\n" +
        "    <p class='simhei bold'>This paragraph requests SimHei Bold (黑体粗体). It should render using Microsoft YaHei Bold.</p>\n" +
        "    <hr>\n" +
        "    <p>This paragraph has no specific font-family, should use body default (YaHei).</p>\n" +
        "    <p class='bold'>This paragraph is bold, should use body default (YaHei Bold).</p>\n" +
        "    <h1>Line-height Test Document</h1>\n" +
        "    <p class='yahei' style='line-height: 1.5;'>This paragraph uses Microsoft YaHei (微软雅黑) explicitly with line-height: 1.5.</p>\n" +
        "    <p class='yahei bold' style='line-height: 1.5;'>This paragraph uses Microsoft YaHei Bold (微软雅黑粗体) explicitly with line-height: 1.5.</p>\n" +
        "    <hr>\n" +
        "    <p class='simsun'>This paragraph requests SimSun (宋体). It should render using Microsoft YaHei.</p>\n" +
        "    <p class='simsun bold'>This paragraph requests SimSun Bold (宋体粗体). It should render using Microsoft YaHei Bold.</p>\n" +
        "    <p class='simsun' style='line-height: 1.5;'>This paragraph requests SimSun (宋体) with line-height: 1.5.</p>\n" +
        "    <div style='width: 50px; height: 50px;'> <span style=\"" + "color: #000;font-family: 宋体;font-size: 9px; line-height: 1.2;font-style: normal;letter-spacing: 0; \">经确认产品与装箱清单吻合，已完成安装并能正常使用</span></div>" +
        "</body>\n" +
        "</html>";
        tempInputHtmlFileForSimpleTest = File.createTempFile("test_input_simple_", ".html");
        tempOutputPdfFileForSimpleTest = new File("test_outputs/test_simple_output.pdf");

        try {
            Files.write(tempInputHtmlFileForSimpleTest.toPath(), simpleHtmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            System.out.println("Simple HTML test input: " + tempInputHtmlFileForSimpleTest.getAbsolutePath());

            try (OutputStream outputStream = new FileOutputStream(tempOutputPdfFileForSimpleTest)) {
                PageSize customPageSize = HtmlToPdfConverter.createPageSizeInMillimeters(100f, 105f);
                HtmlToPdfConverter.convertToPdf(tempInputHtmlFileForSimpleTest, FONT_DIRECTORY, outputStream, customPageSize);
            }

            assertTrue("PDF file from simple HTML should be created", tempOutputPdfFileForSimpleTest.exists());
            assertTrue("PDF file from simple HTML should not be empty", tempOutputPdfFileForSimpleTest.length() > 0);
            System.out.println("PDF from simple HTML CREATED AT: " + tempOutputPdfFileForSimpleTest.getAbsolutePath());

        } finally {
            if (tempInputHtmlFileForSimpleTest != null && tempInputHtmlFileForSimpleTest.exists()) {
                tempInputHtmlFileForSimpleTest.delete(); // Delete the temp HTML input
            }
            // PDF (tempOutputPdfFileForSimpleTest) will NOT be deleted here, so user can inspect it.
            // It will be cleaned up by the tearDown method if needed or manually.
        }
    }

    @Test
    public void testConvertProvidedHtmlToPdf() throws IOException {
        File providedHtmlFile = new File("test.html"); // Assumes test.html is in project root
        
        // Skip test if HTML file does not exist
        Assume.assumeTrue("test.html not found at " + providedHtmlFile.getAbsolutePath() + ", skipping test.", providedHtmlFile.exists());
        System.out.println("Using provided HTML file: " + providedHtmlFile.getAbsolutePath());
        
        // Output PDF to a known directory for easier access
        tempOutputPdfFileForProvidedTest = new File("test_outputs/test_provided_html_output.pdf");

        try (OutputStream outputStream = new FileOutputStream(tempOutputPdfFileForProvidedTest)) {
            PageSize customPageSize = HtmlToPdfConverter.createPageSizeInMillimeters(100f, 105f);
            HtmlToPdfConverter.convertToPdf(providedHtmlFile, FONT_DIRECTORY, outputStream, customPageSize);
        }

        assertTrue("PDF file from provided HTML should be created", tempOutputPdfFileForProvidedTest.exists());
        assertTrue("PDF file from provided HTML should not be empty", tempOutputPdfFileForProvidedTest.length() > 0);
        System.out.println("PDF from " + providedHtmlFile.getName() + " CREATED AT: " + tempOutputPdfFileForProvidedTest.getAbsolutePath());
        
        // PDF (tempOutputPdfFileForProvidedTest) will NOT be deleted here, so user can inspect it.
        // It will be cleaned up by the tearDown method if needed or manually.
    }

    @Test
    public void testConvertExtremelySimpleHtmlToPdf() throws IOException {
        String extremeSimpleHtmlContent = 
        "<!DOCTYPE html>\n" +
        "<html>\n" +
        "<head>\n" +
        "    <meta charset='UTF-8'>\n" +
        "    <title>Extreme Test</title>\n" +
        "    <style>\n" +
        "        html, body {\n" +
        "            width: 100%;\n" +
        "            height: 100%;\n" +
        "            margin: 0;\n" +
        "            padding: 0;\n" +
        "            background-color: lightblue;\n" +
        "            box-sizing: border-box;\n" +
        "            position: absolute;\n" +
        "            top: 0;\n" +
        "            left: 0;\n" +
        "        }\n" +
        "    </style>\n" +
        "</head>\n" +
        "<body>\n" +
        "    Visible Content Test\n" +
        "</body>\n" +
        "</html>";

        tempInputHtmlFileForExtremeTest = File.createTempFile("test_input_extreme_", ".html");
        tempOutputPdfFileForExtremeTest = new File("test_outputs/test_extreme_simple_output.pdf");

        try {
            Files.write(tempInputHtmlFileForExtremeTest.toPath(), extremeSimpleHtmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            System.out.println("Extreme simple HTML test input: " + tempInputHtmlFileForExtremeTest.getAbsolutePath());

            try (OutputStream outputStream = new FileOutputStream(tempOutputPdfFileForExtremeTest)) {
                PageSize customPageSize = HtmlToPdfConverter.createPageSizeInMillimeters(100f, 105f);
                HtmlToPdfConverter.convertToPdf(tempInputHtmlFileForExtremeTest, FONT_DIRECTORY, outputStream, customPageSize);
            }

            assertTrue("PDF file from extreme simple HTML should be created", tempOutputPdfFileForExtremeTest.exists());
            assertTrue("PDF file from extreme simple HTML should not be empty", tempOutputPdfFileForExtremeTest.length() > 0);
            System.out.println("PDF from extreme simple HTML CREATED AT: " + tempOutputPdfFileForExtremeTest.getAbsolutePath());

        } finally {
            if (tempInputHtmlFileForExtremeTest != null && tempInputHtmlFileForExtremeTest.exists()) {
                tempInputHtmlFileForExtremeTest.delete();
            }
            // PDF output is intentionally not deleted for inspection.
        }
    }

    @Test
    public void testPureApiPdfCreation() throws IOException {
        tempOutputPdfFileForApiTest = new File("test_outputs/test_api_generated_output.pdf");
        PageSize customPageSize = HtmlToPdfConverter.createPageSizeInMillimeters(100f, 105f);

        try (PdfWriter writer = new PdfWriter(tempOutputPdfFileForApiTest);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) { // layout.Document

            pdfDoc.setDefaultPageSize(customPageSize); // Set page size on PdfDocument
            document.setMargins(0f, 0f, 0f, 0f);    // Set margins to zero on layout.Document

            // It's important to add a new page if setDefaultPageSize doesn't automatically do it
            // before getting its size or if the document is empty. 
            // However, Document usually handles page creation implicitly when content is added to a new PdfDocument.
            // Let's directly use the dimensions from customPageSize as it's what we set.

            Div fullPageDiv = new Div();
            // Set width and height to the exact dimensions of the page
            fullPageDiv.setWidth(customPageSize.getWidth());
            fullPageDiv.setHeight(customPageSize.getHeight());
            fullPageDiv.setBackgroundColor(ColorConstants.CYAN);
            fullPageDiv.add(new Paragraph("API Generated PDF Test - Cyan Background Should Fill Page"));

            document.add(fullPageDiv);
        }

        assertTrue("PDF file from API test should be created", tempOutputPdfFileForApiTest.exists());
        assertTrue("PDF file from API test should not be empty", tempOutputPdfFileForApiTest.length() > 0);
        System.out.println("PDF from API test CREATED AT: " + tempOutputPdfFileForApiTest.getAbsolutePath());
    }

    @After
    public void tearDown() {
        // This method can be used to clean up files if the tests are set to delete them.
        // For now, we are leaving the PDF outputs for inspection.
        // If you want to re-enable cleanup, uncomment the delete lines.

        if (tempInputHtmlFileForSimpleTest != null && tempInputHtmlFileForSimpleTest.exists()) {
            // This was already deleted in the test method's finally block, but as a safeguard:
            // tempInputHtmlFileForSimpleTest.delete();
        }
        
        // To clean up PDF outputs after inspection, uncomment these lines:
        // if (tempOutputPdfFileForSimpleTest != null && tempOutputPdfFileForSimpleTest.exists()) {
        //     System.out.println("Cleaning up: " + tempOutputPdfFileForSimpleTest.getAbsolutePath());
        //     tempOutputPdfFileForSimpleTest.delete(); 
        // }
        // if (tempOutputPdfFileForProvidedTest != null && tempOutputPdfFileForProvidedTest.exists()) {
        //     System.out.println("Cleaning up: " + tempOutputPdfFileForProvidedTest.getAbsolutePath());
        //     tempOutputPdfFileForProvidedTest.delete(); 
        // }
        System.out.println("Test run finished. PDF outputs (if any) are in 'test_outputs' directory.");
    }
} 