package com.example.fx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private HTMLEditor htmlEditor;

    @FXML
    private void openDocument() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Documents", "*.docx"));
        File file = fileChooser.showOpenDialog(htmlEditor.getScene().getWindow());

        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file)) {
                XWPFDocument document = new XWPFDocument(fis);

                // Создаем HTML-код на основе содержимого документа Word
                String htmlContent = generateHtmlFromDocument(document);

                // Устанавливаем HTML-код в HTMLEditor
                htmlEditor.setHtmlText(htmlContent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String generateHtmlFromDocument(XWPFDocument document) throws IOException {

        StringBuilder htmlBuilder = new StringBuilder();

        // Проходим по всем элементам документа Word
        List<XWPFParagraph> paragraphs = document.getParagraphs();

        for (XWPFParagraph paragraph : paragraphs) {
            String style = getParagraphStyle(paragraph);
            htmlBuilder.append("<p").append(style).append(">").append(paragraph.getText()).append("</p>");
        }

        for (IBodyElement element : document.getBodyElements()) {
            if (element instanceof XWPFPictureData) {

                List<XWPFPictureData> pictures = document.getAllPictures();
                for (XWPFPictureData picture : pictures) {

                    // Преобразование изображения в строку base64
                    String imageData = convertImageToBase64(picture.getData());

                    // Получение размеров изображения
                    int[] dimensions = getImageDimensions(picture.getData());

                    // Вставка изображения в HTML-код с учетом размеров
                    htmlBuilder.append("<img src=\"data:image/png;base64,").append(imageData)
                            .append("\" width=\"").append(dimensions[0]).append("\" height=\"").append(dimensions[1]).append("\">");
                }
            }
        }

        // Проходим по всем таблицам в документе Word
        List<XWPFTable> tables = document.getTables();
        for (XWPFTable table : tables) {
            // Определяем ширину листа документа Word (в дюймах или пикселях)
            // Например, ширина может быть определена через PageWidth
            int pageWidthInPixels = 600; // Пример значения, замените на актуальное

            // Получаем количество колонок в таблице
            int numCols = table.getRow(0).getTableCells().size();

            // Вычисляем ширину каждой колонки (например, можно разделить ширину листа на количество колонок)
            int columnWidth = pageWidthInPixels / numCols;

            // Создаем HTML-код для таблицы с указанием border-collapse: collapse;
            htmlBuilder.append("<table border=\"1\" width=\"").append(columnWidth).append("px\" style=\"border-collapse: collapse;\">"); // Ширина таблицы равна ширине листа

            for (XWPFTableRow row : table.getRows()) {
                htmlBuilder.append("<tr>");
                for (XWPFTableCell cell : row.getTableCells()) {
                    int colspan = getColspan(cell);
                    int rowspan = getRowspan(cell);

                    String cellStyle = getParagraphStyle(cell.getParagraphs().get(0)); // Получаем стиль для ячейки
// Получаем выравнивание текста в ячейке
//                    String alignmentStyle = getParagraphAlignment(cell.getParagraphs().get(0));

                    // Обработка текста внутри ячейки с учетом стилей, объединения ячеек и выравнивания текста
//                    htmlBuilder.append("<td").append(cellStyle).append(alignmentStyle);
                    // Обработка текста внутри ячейки с учетом стилей и объединения ячеек
                    htmlBuilder.append("<td").append(cellStyle);

                    if (colspan > 1) {
                        htmlBuilder.append(" colspan=\"").append(colspan).append("\"");
                    }

                    if (rowspan > 1) {
                        htmlBuilder.append(" rowspan=\"").append(rowspan).append("\"");
                    }

                    htmlBuilder.append(">");

                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        String paragraphStyle = getParagraphStyle(paragraph);
                        htmlBuilder.append("<p").append(paragraphStyle).append(">").append(paragraph.getText()).append("</p>");
                    }

                    htmlBuilder.append("</td>");
                }
                htmlBuilder.append("</tr>");
            }
            htmlBuilder.append("</table>");
        }
        return htmlBuilder.toString();
    }

    public String getParagraphAlignment(XWPFParagraph paragraph) {
        String alignmentStyle = "";

        // Получаем выравнивание текста в параграфе
        ParagraphAlignment alignment = paragraph.getAlignment();
        switch (alignment) {
            case LEFT:
                alignmentStyle = "text-align: left;";
                break;
            case RIGHT:
                alignmentStyle = "text-align: right;";
                break;
            case CENTER:
                alignmentStyle = "text-align: center;";
                break;
            case BOTH:
                alignmentStyle = "text-align: justify;";
                break;
            default:
                break;
        }

        return alignmentStyle;
    }

    // Метод для получения уровня отступа абзаца
    private int getIndentationLevel(XWPFParagraph paragraph) {
        // Получаем отступ в твипсах (1/20 дюйма)
        int indentationInTwips = paragraph.getIndentationLeft();
        // 1 дюйм = 1440 твипсов
        double indentationInInches = indentationInTwips / 1440.0;
        // Преобразуем дюймы в пиксели (предполагая 96 пикселей на дюйм)
        int indentationInPixels = (int) (indentationInInches * 96);
        return indentationInPixels;
    }

    private int getColspan(XWPFTableCell cell) {
        // Получаем XML-элемент таблицы ячейки
        CTTc ctTc = cell.getCTTc();
        // Получаем атрибут colspan
        return ctTc.getTcPr() != null && ctTc.getTcPr().getGridSpan() != null ?
                ctTc.getTcPr().getGridSpan().getVal().intValue() : 1;
    }

    private int getRowspan(XWPFTableCell cell) {
        // Получаем XML-элемент таблицы ячейки
        CTTc ctTc = cell.getCTTc();
        // Получаем атрибут rowspan
        return ctTc.getTcPr() != null && ctTc.getTcPr().getVMerge() != null ?
                ctTc.getTcPr().getVMerge().getVal().intValue() : 1;
    }


    private String getParagraphStyle(XWPFParagraph paragraph) {

        StringBuilder style = new StringBuilder();

        List<XWPFRun> runs = paragraph.getRuns();
        if (!runs.isEmpty()) {
            for (XWPFRun run : runs) {
                String text = run.getText(run.getTextPosition()); // Получаем текст из текущего XWPFRun
                if (text != null && !text.trim().isEmpty()) { // Проверяем, что текст не является пустой
                    // Получаем размер шрифта
                    int fontSize = run.getFontSize();
                    if (fontSize > 0) {
                        style.append("font-size:").append(fontSize).append("pt;");
                    }
                    // Получаем название шрифта
                    String fontFamily = run.getFontFamily();
                    if (fontFamily != null && !fontFamily.trim().isEmpty()) {
                        style.append("font-family:").append(fontFamily).append(";");
                    }
                    // Проверяем стили текста
                    if (run.isBold()) {
                        style.append("font-weight:bold;");
                    }
                    if (run.isItalic()) {
                        style.append("font-style:italic;");
                    }
                    if (run.isStrikeThrough()) {
                        style.append("text-decoration:line-through;");
                    }
                    // Получаем отступ абзаца
                    int indentation = getIndentationLevel(paragraph);
                    style.append(getParagraphAlignment(paragraph)).append("margin-left:").append(indentation).append("px;");
                    System.out.println("Text " + text);
                }
            }
        }

        return " style=\"" + style.toString() + "\"";
    }

    private int[] getImageDimensions(byte[] imageData) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
        int width = img.getWidth();
        int height = img.getHeight();
        return new int[]{width, height};
    }

    private String convertImageToBase64(byte[] imageData) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(img, "png", os);
        return java.util.Base64.getEncoder().encodeToString(os.toByteArray());
    }

    @FXML
    private void saveDocument() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Documents", "*.docx"));
        File file = fileChooser.showSaveDialog(htmlEditor.getScene().getWindow());

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                XWPFDocument document = new XWPFDocument();
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();

                // Получаем HTML-код из HTMLEditor
                String htmlContent = htmlEditor.getHtmlText();

                // Записываем HTML-код в документ Word
                run.setText(htmlContent);

                // Сохраняем документ в формате docx
                document.write(fos);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void printDocument() {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null) {
            // Создаем WebView
            WebView webView = new WebView();

            // Получаем HTML-код из HTMLEditor
            String htmlContent = htmlEditor.getHtmlText();

            // Загружаем HTML-код в WebView
            webView.getEngine().loadContent(htmlContent);

            // Настройка параметров печати
            PageLayout pageLayout = printerJob.getJobSettings().getPageLayout();
            double printableWidth = pageLayout.getPrintableWidth();
            double printableHeight = pageLayout.getPrintableHeight();

            // Масштабируем содержимое для печати
            webView.setPrefSize(printableWidth, printableHeight);

            // Выполняем печать
            if (printerJob.showPrintDialog(htmlEditor.getScene().getWindow())) {
                printerJob.printPage(webView);
                printerJob.endJob();
            }
        }
    }

    @FXML
    private void exitApplication() {
        Platform.exit();
    }

    // Скрываем панель форматирования текста htmlEditor
    public void hideHTMLEditorToolbars(final HTMLEditor editor) {
        editor.setVisible(false);
        Platform.runLater(() -> {
            Node[] nodes = editor.lookupAll(".tool-bar").toArray(new Node[0]);
            for (Node node : nodes) {
                node.setVisible(false);
                node.setManaged(false);
            }
            editor.setVisible(true);
        });
    }

    //Вызываем метод после инициализации
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hideHTMLEditorToolbars(htmlEditor);
    }
}
