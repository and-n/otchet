package ru.rzd.dayresult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.rzd.otchet.Pair;

/**
 *
 * @author ATonevitskiy
 */
public class DayResultLogic {

    public void createReport(Calendar date) throws FileNotFoundException, IOException, Exception {
        File sh = new File("folder" + File.separator + "ДИСПЕТЧЕРА.xlsx");
        if (!sh.exists()) {
            throw new FileNotFoundException("Не найден файл шаблона.");
        }
        Workbook wb = new XSSFWorkbook(new FileInputStream(sh));
        Sheet sheet1 = wb.getSheetAt(0);
        if (sheet1 == null) {
            throw new Exception("Изменился шаблон - не попали в нужный лист.");
        }
        List<Pair<Row, String>> fio = findFIO(sheet1);
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
            DayResultTask dtast = new DayResultTask(operator, dao, p.getL());
            flist.add(executor.submit(dtast));

        }

        for (Future f : flist) {
            f.get();
        }
        executor.shutdown();
//        addOperatorToSheet(sheet1, operator);
    }

    private List<Pair<Row, String>> findFIO(Sheet sheet1) {
        List<Pair<Row, String>> res = new ArrayList<>();
        Row r = sheet1.getRow(3);
        for (int i = 4; r != null; i++) {
            String fio = r.getCell(0).getStringCellValue().toUpperCase();
            if (!fio.startsWith("ИТОГО") && !fio.startsWith("ПЛОЩАДКА")) {
                res.add(new Pair<Row, String>(r, fio));
            }
            r = sheet1.getRow(i);
        }
        return res;
    }

}
