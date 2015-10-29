package ru.rzd.dayresult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.JOptionPane;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.rzd.otchet.Form;
import static ru.rzd.otchet.Form.ISCONSOLE;
import ru.rzd.otchet.Pair;

/**
 *
 * @author ATonevitskiy
 */
public class DayResultLogic {
    
    public void createReport(Calendar date, Calendar end, boolean isPeriod, Form f) throws FileNotFoundException, IOException, Exception {
        if (isPeriod) {
            JOptionPane.showMessageDialog(null, "Пока не готово!");
        } else if (end != null && end.after(date)) {
            boolean b = Form.ISCONSOLE;
            ISCONSOLE = true;
            while (date.before(end) || date.equals(end)) {
                Sheet sheet1 = getSheet();
                List< Pair< Row, String>> fio = findFIO(sheet1);
                getInfo(date, fio);
                saveReport(sheet1, f, date);
                date.setTimeInMillis(date.getTimeInMillis() + 86400000L);
            }
            ISCONSOLE = b;
        } else {
            Sheet sheet1 = getSheet();
            List< Pair< Row, String>> fio = findFIO(sheet1);
            getInfo(date, fio);
            saveReport(sheet1, f, date);
        }
    }
    
    private List<Pair<Row, String>> findFIO(Sheet sheet1) {
        List<Pair<Row, String>> res = new ArrayList<>();
        Row r = sheet1.getRow(3);
        for (int i = 4; r != null && r.getCell(0) != null; i++) {
            String fio = r.getCell(0).getStringCellValue().toUpperCase();
            if (!fio.startsWith("ИТОГО") && !fio.startsWith("ПЛОЩАДКА")) {
                res.add(new Pair<Row, String>(r, fio));
            }
            r = sheet1.getRow(i);
        }
        return res;
    }
    
    private String getFileName(String day_report, Calendar date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
        return day_report + sdf.format(date.getTime());
    }
    
    private Sheet getSheet() throws FileNotFoundException, IOException, Exception {
        File sh = new File("folder" + File.separator + "ДИСПЕТЧЕРА.xlsx");
        if (!sh.exists()) {
            throw new FileNotFoundException("Не найден файл шаблона.");
        }
        Workbook wb = new XSSFWorkbook(new FileInputStream(sh));
        Sheet sheet1 = wb.getSheetAt(0);
        if (sheet1 == null) {
            throw new Exception("Изменился шаблон - не попали в нужный лист.");
        }
        return sheet1;
    }
    
    private void getInfo(Calendar date, List< Pair< Row, String>> fio) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<Void>> flist = new ArrayList<>();
        for (Pair<Row, String> p : fio) {
            String[] init = p.getR().split("\\s+");
            if (init.length != 3) {
                throw new Exception("ФИО не соответсвует ожиданию: " + p.getR());
            }
            String surname = init[0].toUpperCase().charAt(0) + init[0].toUpperCase().toLowerCase().substring(1);
            Operator operator = new Operator(p.getR().substring(surname.length()), surname);
            DAODayResult dao = new DAODayResult();
            DayResultTask dtast = new DayResultTask(operator, dao, p.getL(), date);
            flist.add(executor.submit(dtast));
        }
        for (Future future : flist) {
            future.get();
        }
        executor.shutdown();
    }
    
    private void saveReport(Sheet sheet1, Form f, Calendar date) throws FileNotFoundException, IOException {
        String fileName = getFileName("Показатели работы операторов ", date);
        String folder = "";
        if (ISCONSOLE) {
            folder = "OperatorReports";
            new File(folder).mkdir();
        } else {
            folder = f.selectSaveFile();
        }
        if (!folder.isEmpty()) {
            FormulaEvaluator formulaEvaluator = sheet1.getWorkbook().getCreationHelper().createFormulaEvaluator();
            formulaEvaluator.evaluateAll();
            DateFormat df1 = new SimpleDateFormat("dd.MM.yyyy");
            sheet1.getRow(1).getCell(0).setCellValue(df1.format(date.getTime()));
            FileOutputStream fos = new FileOutputStream(folder + File.separator + fileName + ".xlsx", false);
            sheet1.getWorkbook().write(fos);
            sheet1.getWorkbook().close();
            fos.close();
        }
    }
    
}
