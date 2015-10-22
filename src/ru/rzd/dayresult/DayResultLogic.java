/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
        List<String> fio = findFIO(sheet1);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<Operator>> flist = new ArrayList<>();
        for (String f : fio) {
            String[] init = f.split("\\s+");
            if (init.length != 3) {
                throw new Exception("ФИО не соответсвует ожиданию: " + f);
            }
            String surname = init[0].toUpperCase().charAt(0) + init[0].toUpperCase().toLowerCase().substring(1);
            Operator operator = new Operator(f.substring(surname.length()), surname);
            DAODayResult dao = new DAODayResult();
            DayResultTask dtast = new DayResultTask(operator, dao);
            flist.add(executor.submit(dtast));

        }

        for (Future f : flist) {
            f.get();
        }
        executor.shutdown();
//        addOperatorToSheet(sheet1, operator);
    }

    private List<String> findFIO(Sheet sheet1) {
        List<String> res = new ArrayList<>();
        Row r = sheet1.getRow(3);
        for (int i = 4; r != null; i++) {
            String fio = r.getCell(0).getStringCellValue().toUpperCase();
            if (!fio.startsWith("ИТОГО") && !fio.startsWith("ПЛОЩАДКА")) {
                res.add(fio);
            }
            r = sheet1.getRow(i);
        }
        return res;
    }

}
