package com.example.pdf;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.io.font.PdfEncodings;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PageHeaderEventHandler implements IEventHandler {

    private static final Logger LOGGER = Logger.getLogger(PageHeaderEventHandler.class.getName());
    private String pageHeaderContent;

    public PageHeaderEventHandler(String pageHeaderContent) {
        this.pageHeaderContent = pageHeaderContent;
    }

    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent pdfDocumentEvent = (PdfDocumentEvent) event;
        PdfPage page = pdfDocumentEvent.getPage();
        Rectangle pageSize = page.getPageSize();
        PdfDocument pdfDoc = pdfDocumentEvent.getDocument();

        // 不在第一页添加页眉
        if (pdfDoc.getPageNumber(page) != 1) {
            PdfFont pdfFont = null;
            try {
                // 尝试使用一个标准中文字体
                pdfFont = PdfFontFactory.createFont("STSongStd-Light", PdfEncodings.IDENTITY_H);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "STSongStd-Light font for page header not found, using default font.", e);
                try {
                     pdfFont = PdfFontFactory.createFont(); // Default font
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Default font for page header could not be created.", ex);
                    return; // Cannot proceed without a font
                }
            }

            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
            // Use try-with-resources for Canvas
            try (Canvas canvas = new Canvas(pdfCanvas, pageSize)) {
                float x = (pageSize.getLeft() + pageSize.getRight()) / 2;
                float y = pageSize.getTop() - 20;
                Paragraph paragraph = new Paragraph(pageHeaderContent)
                        .setFontSize(10)
                        .setFont(pdfFont);
                canvas.showTextAligned(paragraph, x, y, TextAlignment.CENTER);
            } // canvas is closed here
            
            // 在页面顶部绘制分隔线
            pdfCanvas.setStrokeColor(new DeviceRgb(60, 60, 60))
                    .setLineWidth(0.5f)
                    .moveTo(pageSize.getLeft() + 80, pageSize.getTop() - 25)
                    .lineTo(pageSize.getRight() - 80, pageSize.getTop() - 25)
                    .stroke();
            // No explicit pdfCanvas.release() needed if itext handles it or if it's tied to page stream lifecycle.
            // Original code didn't have release after moving it out of canvas.close()
        }
    }
} 