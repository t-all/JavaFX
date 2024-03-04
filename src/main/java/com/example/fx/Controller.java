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
        System.out.println("***************" + htmlBuilder);
        return htmlBuilder.toString();
    }

    public String getParagraphAlignment(XWPFParagraph paragraph) {
        String alignmentStyle = "";

        // Получаем выравнивание текста в параграфе
        ParagraphAlignment alignment = paragraph.getAlignment();
        System.out.println("alignment " + alignment);
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

        // Получаем стиль параграфа
        List<XWPFRun> runs = paragraph.getRuns();
        if (!runs.isEmpty()) {
            // Получаем первый объект XWPFRun из параграфа
            XWPFRun run = runs.get(0);
            // Получаем размер шрифта
            int fontSize = run.getFontSize();
            if (fontSize > 0) {
                style.append("font-size:").append(fontSize).append("pt;");
            }
            // Получаем название шрифта
            String fontFamily = run.getFontFamily();
            if (fontFamily != null && !fontFamily.isEmpty()) {
                style.append("font-family:").append(fontFamily).append(";");
            }
            // Проверяем стили текста
            boolean isBold = false;
            boolean isItalic = false;
            boolean isStrikeThrough = false;

            for (XWPFRun runF : runs) {
                if (runF.isBold()) {
                    isBold = true;
                }
                if (runF.isItalic()) {
                    isItalic = true;
                }
                if (runF.isStrikeThrough()) {
                    isStrikeThrough = true;
                }
            }

            // Добавляем стили текста
            if (isBold) {
                style.append("font-weight:bold;");
            }
            if (isItalic) {
                style.append("font-style:italic;");
            }
            if (isStrikeThrough) {
                style.append("text-decoration:line-through;");
            }
            style.append(getParagraphAlignment(paragraph));
        }

        return " style=\"" + style.toString() + "\"";
    }

//    @FXML
//    private void openDocument() {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Documents", "*.docx"));
//        File file = fileChooser.showOpenDialog(htmlEditor.getScene().getWindow());
//
//        if (file != null) {
//            try (FileInputStream fis = new FileInputStream(file)) {
//                XWPFDocument document = new XWPFDocument(fis);
//                StringBuilder htmlContent = new StringBuilder();
//
//
//                // Проход по содержимому документа Word
//                for (IBodyElement element : document.getBodyElements()) {
//                    if (element instanceof XWPFParagraph) {
//
//                        // Обработка абзаца
//                        XWPFParagraph paragraph = (XWPFParagraph) element;
//                        htmlContent.append("<p>").append(paragraph.getText()).append("</p>");
//                        System.out.println("**********" + paragraph.getText());
//                        System.out.println("HTML_CONTENT" + htmlContent);
//
//
//                    } else if (element instanceof XWPFTable) {
//                        List<XWPFTable> tables = document.getTables();
//                        for (XWPFTable table : tables) {
//                            // Добавляем HTML-код для таблицы
//                            htmlContent.append("<table border=\"1\">"); // Добавляем границы таблицы
//                            for (XWPFTableRow row : table.getRows()) {
//                                htmlContent.append("<tr>");
//                                for (XWPFTableCell cell : row.getTableCells()) {
//                                    // Добавляем HTML-код для ячеек таблицы
//                                    htmlContent.append("<td>").append(cell.getText()).append("</td>");
//                                }
//                                htmlContent.append("</tr>");
//                            }
//                            htmlContent.append("</table>");
//                        }
////                        XWPFTable table = (XWPFTable) element;
////                        htmlContent.append("<table>");
////                        for (XWPFTableRow row : table.getRows()) {
////                            htmlContent.append("<tr>");
////                            for (XWPFTableCell cell : row.getTableCells()) {
////                                htmlContent.append("<td>").append(cell.getText()).append("</td>");
////                            }
////                            htmlContent.append("</tr>");
////                        }
////                        htmlContent.append("</table>");
//                    } else /*if (element instanceof XWPFPictureData)*/ {
//
//                        List<XWPFPictureData> pictures = document.getAllPictures();
//                        for (XWPFPictureData picture : pictures) {
//                            // Преобразование изображения в строку base64
//                            String imageData = convertImageToBase64(picture.getData());
//// Получение размеров изображения
//                            int[] dimensions = getImageDimensions(picture.getData());
//
//                            // Вставка изображения в HTML-код с учетом размеров
//                            htmlContent.append("<img src=\"data:image/png;base64,").append(imageData)
//                                    .append("\" width=\"").append(dimensions[0]).append("\" height=\"").append(dimensions[1]).append("\">");
//                        }
//                    }}
//                    for (XWPFParagraph paragraph : document.getParagraphs()) {
//                        int fontSize = -1;
//                        boolean isBold;
//                        boolean isItalic;
//
//                        for (XWPFRun run : paragraph.getRuns()) {
//                            String text = run.getText(0);
//                            if (text != null && !text.isEmpty()) {
//                                // Получаем информацию о шрифте из объекта XWPFRun
//                                String fontFamily = paragraph.getRuns().get(0).getFontFamily();
////                            if (fontFamily == null || fontFamily.isEmpty()) {
////                                fontFamily = "Arial";
////                            }
//                                System.out.println(fontSize);
//                                System.out.println(fontFamily);
//                                fontSize = run.getFontSize();
//                                isBold = run.isBold();
//                                isItalic = run.isItalic();
//
//                                // Создаем HTML тег с текстом и примененными стилями
//                                htmlContent.append("<span style=\"");
//
////                            if (fontFamily != null) {
//                                htmlContent.append("font-family: ").append(fontFamily).append(";");
////                            }
////                            if (fontSize != -1) {
//                                htmlContent.append("font-size: ").append(fontSize).append("pt;");
////                            }
//                                if (isBold) {
//                                    htmlContent.append("font-weight: bold;");
//                                }
//                                if (isItalic) {
//                                    htmlContent.append("font-style: italic;");
//                                }
//
//                                htmlContent.append("\">").append(text).append("</span>");
//                            }
//                        }
//
//                        // Добавляем разрыв строки между абзацами
//                        htmlContent.append("<br>");
//
//                }
//
//                String html = "<html><body>" + htmlContent + "</body></html>";
//                htmlEditor.setHtmlText(html);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

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
