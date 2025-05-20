package com.example.pdf;

import com.itextpdf.kernel.geom.PageSize;
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
        String simpleHtmlContent = "<!DOCTYPE html><html><head><title>Test</title><meta charset='UTF-8'></head><body><h1>Hello World - English</h1><p>This is a simple HTML to PDF test.</p><p>你好世界 - Chinese Characters (Default Font Test)</p><h2>Test Table</h2><table border='1'><tr><th>Header 1</th><th>Header 2</th></tr><tr><td>Data 1.1</td><td>Data 1.2</td></tr><tr><td>数据 2.1</td><td>数据 2.2</td></tr></table><hr><p style='font-family:微软雅黑;'>This should be Microsoft YaHei (微软雅黑)</p><p style='font-family:黑体;'>This should be SimHei (黑体)</p><p style='font-family:宋体;'>This should be SimSun or mapped to YaHei (宋体)</p></body></html>";
        tempInputHtmlFileForSimpleTest = File.createTempFile("test_input_simple_", ".html");
        tempOutputPdfFileForSimpleTest = new File("test_outputs/test_simple_output.pdf");

        try {
            Files.write(tempInputHtmlFileForSimpleTest.toPath(), simpleHtmlContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            System.out.println("Simple HTML test input: " + tempInputHtmlFileForSimpleTest.getAbsolutePath());

            try (OutputStream outputStream = new FileOutputStream(tempOutputPdfFileForSimpleTest)) {
                HtmlToPdfConverter.convertToPdf(tempInputHtmlFileForSimpleTest, FONT_DIRECTORY, outputStream, PageSize.A4);
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
            HtmlToPdfConverter.convertToPdf(providedHtmlFile, FONT_DIRECTORY, outputStream, PageSize.A4);
        }

        assertTrue("PDF file from provided HTML should be created", tempOutputPdfFileForProvidedTest.exists());
        assertTrue("PDF file from provided HTML should not be empty", tempOutputPdfFileForProvidedTest.length() > 0);
        System.out.println("PDF from " + providedHtmlFile.getName() + " CREATED AT: " + tempOutputPdfFileForProvidedTest.getAbsolutePath());
        
        // PDF (tempOutputPdfFileForProvidedTest) will NOT be deleted here, so user can inspect it.
        // It will be cleaned up by the tearDown method if needed or manually.
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