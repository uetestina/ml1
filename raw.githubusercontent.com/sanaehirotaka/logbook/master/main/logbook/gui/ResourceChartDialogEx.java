package logbook.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.gui.listener.SaveWindowLocationAdapter;
import logbook.gui.listener.SelectedListener;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.LayoutLogic;
import logbook.util.ImageWriter;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * ????????????????????????????????????
 *
 */
public final class ResourceChartDialogEx extends Dialog {

    static {
        Platform.setImplicitExit(false);
    }

    private static class LoggerHolder {
        /** ????????? */
        private static final Logger LOG = LogManager.getLogger(ResourceChartDialogEx.class);
    }

    /** ???????????????????????????????????????????????????????????? */
    private static final String COMPARE_FORMAT = "{0,number,0}({1,number,+0;-0})";

    private Shell shell;
    private NumberAxis xaxis;
    private NumberAxis yaxis;
    private LineChart<Number, Number> chart;
    private Combo combo;
    private DateTime dateTimeFrom;
    private DateTime dateTimeTo;
    private Button fuelBtn;
    private Button ammoBtn;
    private Button metalBtn;
    private Button bauxiteBtn;
    private Button bucketBtn;
    private Button burnerBtn;
    private Button researchBtn;
    private FXCanvas fxCanvas;
    private Button forceZeroBtn;
    private Table table;
    /** ?????????????????????????????? */
    private final String[] header = Arrays.copyOfRange(CreateReportLogic.getMaterialHeader(), 1, 9);
    /** ?????????????????????????????? */
    private final List<String[]> body = new ArrayList<>();

    /**
     * Create the dialog.
     * @param parent
     */
    public ResourceChartDialogEx(Shell parent) {
        super(parent, SWT.SHELL_TRIM | SWT.MODELESS);
        this.setText("??????????????????");
    }

    /**
     * Open the dialog.
     */
    public void open() {
        this.createContents();
        this.shell.open();
        this.shell.layout();
        Display display = this.getParent().getDisplay();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        this.shell = new Shell(this.getParent(), this.getStyle());
        this.shell.setMinimumSize(450, 300);
        this.shell.setSize(800, 650);
        this.shell.setText(this.getText());

        // ??????????????????????????????
        LayoutLogic.applyWindowLocation(this.getClass(), this.shell);
        // ?????????????????????????????????????????????
        this.shell.addShellListener(new SaveWindowLocationAdapter(this.getClass()));
        this.shell.setLayout(new GridLayout(1, false));

        SashForm sashForm = new SashForm(this.shell, SWT.SMOOTH | SWT.VERTICAL);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        Composite mainComposite = new Composite(sashForm, SWT.NONE);
        mainComposite.setLayout(new GridLayout(1, false));

        Composite rangeComposite = new Composite(mainComposite, SWT.NONE);
        rangeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        rangeComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

        Label label1 = new Label(rangeComposite, SWT.NONE);
        label1.setText("??????");
        this.combo = new Combo(rangeComposite, SWT.READ_ONLY);
        this.combo.setItems(
                Arrays.stream(ScaleOption.values())
                        .map(e -> e.toString())
                        .toArray(String[]::new)
                );
        this.combo.select(2);
        this.combo.addSelectionListener((SelectedListener) e -> {
            this.setRange();
            this.reload();
        });

        Label label2 = new Label(rangeComposite, SWT.NONE);
        label2.setText("??????");
        this.dateTimeFrom = new DateTime(rangeComposite, SWT.BORDER | SWT.DROP_DOWN);
        this.dateTimeFrom.addSelectionListener((SelectedListener) e -> this.reload());
        Label label3 = new Label(rangeComposite, SWT.NONE);
        label3.setText("??????");
        this.dateTimeTo = new DateTime(rangeComposite, SWT.BORDER | SWT.DROP_DOWN);
        this.dateTimeTo.addSelectionListener((SelectedListener) e -> this.reload());

        Composite checkComposite = new Composite(mainComposite, SWT.NONE);
        checkComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        checkComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

        this.fuelBtn = new Button(checkComposite, SWT.CHECK);
        this.fuelBtn.setText("??????");
        this.fuelBtn.setSelection(true);
        this.fuelBtn.addSelectionListener((SelectedListener) e -> this.reload());
        this.ammoBtn = new Button(checkComposite, SWT.CHECK);
        this.ammoBtn.setText("??????");
        this.ammoBtn.setSelection(true);
        this.ammoBtn.addSelectionListener((SelectedListener) e -> this.reload());
        this.metalBtn = new Button(checkComposite, SWT.CHECK);
        this.metalBtn.setText("??????");
        this.metalBtn.setSelection(true);
        this.metalBtn.addSelectionListener((SelectedListener) e -> this.reload());
        this.bauxiteBtn = new Button(checkComposite, SWT.CHECK);
        this.bauxiteBtn.setText("?????????");
        this.bauxiteBtn.setSelection(true);
        this.bauxiteBtn.addSelectionListener((SelectedListener) e -> this.reload());
        this.bucketBtn = new Button(checkComposite, SWT.CHECK);
        this.bucketBtn.setText("???????????????");
        this.bucketBtn.addSelectionListener((SelectedListener) e -> this.reload());
        this.burnerBtn = new Button(checkComposite, SWT.CHECK);
        this.burnerBtn.setText("???????????????");
        this.burnerBtn.addSelectionListener((SelectedListener) e -> this.reload());
        this.researchBtn = new Button(checkComposite, SWT.CHECK);
        this.researchBtn.setText("????????????");
        this.researchBtn.addSelectionListener((SelectedListener) e -> this.reload());
        this.forceZeroBtn = new Button(checkComposite, SWT.CHECK);
        this.forceZeroBtn.setText("???????????????");
        this.forceZeroBtn.addSelectionListener((SelectedListener) e -> {
            this.yaxis.setForceZeroInRange(this.forceZeroBtn.getSelection());
            this.reload();
        });

        try {
            // Create an FXCanvas
            this.fxCanvas = new FXCanvas(mainComposite, SWT.NONE);
            this.fxCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
            VBox group = new VBox();

            this.xaxis = new NumberAxis();
            this.yaxis = new NumberAxis();
            this.yaxis.setForceZeroInRange(false);

            this.chart = new LineChart<Number, Number>(this.xaxis, this.yaxis);
            this.chart.setPrefHeight(800);
            this.chart.setCreateSymbols(false);
            // ???????????????????????????????????????????????????axis????????????????????????????????????
            this.chart.setAnimated(false);
            this.chart.getStylesheets().add(AppConstants.CHART_STYLESHEET_FILE.toUri().toString());
            group.getChildren().add(this.chart);
            // ????????????????????????
            this.setRange();
            this.reload();

            Scene scene = new Scene(group, Color.rgb(
                    this.shell.getBackground().getRed(),
                    this.shell.getBackground().getGreen(),
                    this.shell.getBackground().getBlue()));
            this.fxCanvas.setScene(scene);

            Menu menu = new Menu(this.fxCanvas);
            this.fxCanvas.setMenu(menu);
            MenuItem saveimage = new MenuItem(menu, SWT.NONE);
            saveimage.addSelectionListener((SelectedListener) e -> {
                try {
                    FileDialog dialog = new FileDialog(this.shell, SWT.SAVE);
                    dialog.setFileName("??????????????????.png");
                    dialog.setFilterExtensions(new String[] { "*.png" });
                    String filename = dialog.open();
                    if (filename != null) {
                        Path path = Paths.get(filename);
                        if (Files.exists(path)) {
                            MessageBox messageBox = new MessageBox(this.shell, SWT.YES
                                    | SWT.NO);
                            messageBox.setText("??????");
                            messageBox.setMessage("????????????????????????????????????????????????\n????????????????????????");
                            if (messageBox.open() == SWT.NO) {
                                return;
                            }
                        }
                        new ImageWriter(path)
                                .format(SWT.IMAGE_PNG)
                                .write(this.fxCanvas);
                    }
                } catch (IOException ex) {
                    LoggerHolder.LOG.warn("???????????????????????????????????????????????????????????????????????????", ex);
                }
            });
            saveimage.setText("?????????????????????????????????");
        } catch (Exception e) {
            LoggerHolder.LOG.warn("????????????????????????????????????????????????????????????", e);
        }

        Composite compositeTable = new Composite(sashForm, SWT.NONE);
        compositeTable.setLayout(new GridLayout(1, false));

        this.table = new Table(compositeTable, SWT.BORDER | SWT.FULL_SELECTION);
        this.table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        this.table.setHeaderVisible(true);
        this.table.setLinesVisible(true);
        sashForm.setWeights(new int[] { 3, 1 });

        // ???????????????????????????
        this.setTableHeader();
        try {
            createTableBody(Paths.get(AppConfig.get().getReportPath(),
                    AppConstants.LOG_RESOURCE), this.body);
            this.setTableBody();
            this.packTableHeader();
        } catch (Exception e) {
            LoggerHolder.LOG.warn("??????????????????????????????????????????????????????????????????", e);
        }
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????
     */
    private void reload() {
        this.changeRange();
    }

    /**
     * ?????????????????????????????????????????????????????????
     */
    private void setRange() {
        int idx = this.combo.getSelectionIndex();
        int days = ScaleOption.values()[idx].getDay() - 1;
        // To?????????
        Calendar base = getCalendar(this.dateTimeTo);
        // ??????????????????????????????????????????????????????????????????
        base.add(Calendar.DAY_OF_YEAR, -days);
        // From?????????
        setCalendar(base, this.dateTimeFrom);
    }

    /**
     * ????????????????????????????????????
     */
    private void changeRange() {
        int idx = this.combo.getSelectionIndex();
        ScaleOption option = ScaleOption.values()[idx];

        // ??????
        Date from = getCalendar(this.dateTimeFrom).getTime();
        // ??????
        Calendar calTo = getCalendar(this.dateTimeTo);
        // ??????????????????????????????????????????23:59.59???????????????1???????????????
        calTo.add(Calendar.DAY_OF_YEAR, 1);
        Date to = calTo.getTime();

        this.xaxis.setAutoRanging(false);
        this.xaxis.setLowerBound(0);
        this.xaxis.setUpperBound(to.getTime() - from.getTime());
        this.xaxis.setTickUnit(option.getTickUnit());
        this.xaxis.setTickLabelFormatter(new DateTimeConverter(from, option.getFormat()));
        this.loadSeries(from, to);
    }

    /**
     * ???????????????????????????
     *
     * @param from ??????(???????????????)
     * @param to ??????(?????????????????????)
     */
    private void loadSeries(Date from, Date to) {
        // Series?????????add?????????1???add???????????????XYChart$Series$1.onChanged()????????????????????????????????????????????????????????????ArrayList??????????????????addAll??????
        List<XYChart.Data<Number, Number>> fuelList = new ArrayList<>();
        List<XYChart.Data<Number, Number>> ammoList = new ArrayList<>();
        List<XYChart.Data<Number, Number>> metalList = new ArrayList<>();
        List<XYChart.Data<Number, Number>> bauxiteList = new ArrayList<>();
        List<XYChart.Data<Number, Number>> bucketList = new ArrayList<>();
        List<XYChart.Data<Number, Number>> burnerList = new ArrayList<>();
        List<XYChart.Data<Number, Number>> researchList = new ArrayList<>();
        try {
            try (Stream<String> stream = Files.lines(
                    Paths.get(AppConfig.get().getReportPath(), AppConstants.LOG_RESOURCE), AppConstants.CHARSET)) {
                stream.skip(1)
                        .map(Log::new)
                        .filter(e -> e.date != null)
                        .filter(e -> e.date.compareTo(from) >= 0)
                        .filter(e -> e.date.compareTo(to) < 0)
                        .forEach(
                                e -> {
                                    long time = e.date.getTime() - from.getTime();
                                    fuelList.add(new XYChart.Data<Number, Number>(time, e.fuel));
                                    ammoList.add(new XYChart.Data<Number, Number>(time, e.ammo));
                                    metalList.add(new XYChart.Data<Number, Number>(time, e.metal));
                                    bauxiteList.add(new XYChart.Data<Number, Number>(time, e.bauxite));
                                    bucketList.add(new XYChart.Data<Number, Number>(time, e.bucket));
                                    burnerList.add(new XYChart.Data<Number, Number>(time, e.burner));
                                    researchList.add(new XYChart.Data<Number, Number>(time, e.research));
                                });
            }
        } catch (Exception e) {
            LoggerHolder.LOG.warn("??????????????????????????????????????????????????????????????????", e);
        }

        XYChart.Series<Number, Number> fuel = new XYChart.Series<>();
        XYChart.Series<Number, Number> ammo = new XYChart.Series<>();
        XYChart.Series<Number, Number> metal = new XYChart.Series<>();
        XYChart.Series<Number, Number> bauxite = new XYChart.Series<>();
        XYChart.Series<Number, Number> bucket = new XYChart.Series<>();
        XYChart.Series<Number, Number> burner = new XYChart.Series<>();
        XYChart.Series<Number, Number> research = new XYChart.Series<>();

        fuel.setName("??????");
        ammo.setName("??????");
        metal.setName("??????");
        bauxite.setName("?????????");
        bucket.setName("???????????????");
        burner.setName("???????????????");
        research.setName("????????????");

        if (this.fuelBtn.getSelection())
            fuel.getData().addAll(fuelList);
        if (this.ammoBtn.getSelection())
            ammo.getData().addAll(ammoList);
        if (this.metalBtn.getSelection())
            metal.getData().addAll(metalList);
        if (this.bauxiteBtn.getSelection())
            bauxite.getData().addAll(bauxiteList);
        if (this.bucketBtn.getSelection())
            bucket.getData().addAll(bucketList);
        if (this.burnerBtn.getSelection())
            burner.getData().addAll(burnerList);
        if (this.researchBtn.getSelection())
            research.getData().addAll(researchList);

        this.chart.getData().clear();
        List<XYChart.Series<Number, Number>> list = new ArrayList<>();
        list.add(fuel);
        list.add(ammo);
        list.add(metal);
        list.add(bauxite);
        list.add(bucket);
        list.add(burner);
        list.add(research);
        this.chart.getData().addAll(list);
    }

    /**
     * ??????????????????????????????????????????
     */
    private void setTableHeader() {
        for (int i = 0; i < this.header.length; i++) {
            TableColumn col = new TableColumn(this.table, SWT.LEFT);
            col.setText(this.header[i]);
        }
        this.packTableHeader();
    }

    /**
     * ?????????????????????????????????????????????
     */
    private void packTableHeader() {
        TableColumn[] columns = this.table.getColumns();
        for (int i = 0; i < columns.length; i++) {
            columns[i].pack();
        }
    }

    /**
     * ??????????????????????????????????????????
     */
    private void setTableBody() {
        for (int i = 0; i < this.body.size(); i++) {
            String[] line = this.body.get(i);
            TableItem item = new TableItem(this.table, SWT.NONE);
            item.setText(line);
            // ?????????????????????????????????
            if ((i % 2) != 0) {
                item.setBackground(SWTResourceManager.getColor(AppConstants.ROW_BACKGROUND));
            } else {
                item.setBackground(null);
            }
        }
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param log ????????????
     * @param body ?????????????????????
     * @throws IOException
     */
    private static void createTableBody(Path path, List<String[]> body) throws IOException {

        SimpleDateFormat format = new SimpleDateFormat(AppConstants.DATE_DAYS_FORMAT);
        format.setTimeZone(AppConstants.TIME_ZONE_MISSION);

        try (Stream<String> stream = Files.lines(path, AppConstants.CHARSET)) {
            BiConsumer<Map<String, Log>, Log> accumulator = (t, u) -> {
                String key = format.format(u.date);
                Log get = t.get(key);
                if ((get == null) || (get.date.compareTo(u.date) < 0))
                    t.put(key, u);
            };
            BiConsumer<Map<String, Log>, Map<String, Log>> combiner = (t, u) -> {
                for (Entry<String, Log> entry : u.entrySet()) {
                    String key = format.format(entry.getValue().date);
                    Log get = t.get(key);
                    if ((get == null) || (get.date.compareTo(entry.getValue().date) < 0))
                        t.put(key, entry.getValue());
                }
            };
            List<Entry<String, Log>> list = stream.skip(1)
                    .map(Log::new)
                    .filter(e -> e.date != null)
                    .collect(HashMap<String, Log>::new, accumulator, combiner)
                    .entrySet()
                    .stream()
                    .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                    .collect(Collectors.toList());

            MessageFormat compare = new MessageFormat(COMPARE_FORMAT);
            Log before = null;
            for (Entry<String, Log> entry : list) {
                Log val = entry.getValue();
                int[] material = { val.fuel, val.ammo, val.metal, val.bauxite, val.bucket, val.burner, val.research };
                int[] materialCompare = new int[material.length];
                if (before != null) {
                    int[] materialBefore = { before.fuel, before.ammo, before.metal, before.bauxite, before.bucket,
                            before.burner, before.research };
                    for (int i = 0; i < material.length; i++) {
                        materialCompare[i] = material[i] - materialBefore[i];
                    }
                }
                before = val;
                String[] line = new String[material.length + 1];
                line[0] = entry.getKey();
                for (int i = 0; i < material.length; i++) {
                    line[i + 1] = compare.format(new Object[] { material[i], materialCompare[i] });
                }
                body.add(line);
            }
        }
        Collections.reverse(body);
    }

    /**
     * DateTime????????????????????????????????????Calendar????????????????????????????????????
     *
     * @param dateTime
     * @return
     */
    private static Calendar getCalendar(DateTime dateTime) {
        Calendar cal = DateUtils.truncate(Calendar.getInstance(TimeZone.getDefault()), Calendar.DAY_OF_MONTH);
        cal.set(Calendar.YEAR, dateTime.getYear());
        cal.set(Calendar.MONTH, dateTime.getMonth());
        cal.set(Calendar.DAY_OF_MONTH, dateTime.getDay());
        return cal;
    }

    /**
     * DateTime???Calendar?????????????????????????????????
     *
     * @param cal
     * @param dateTime
     */
    private static void setCalendar(Calendar cal, DateTime dateTime) {
        dateTime.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * ????????????
     *
     */
    private static class Log {
        /** ?????? */
        private Date date;
        /** ?????? */
        private int fuel;
        /** ?????? */
        private int ammo;
        /** ?????? */
        private int metal;
        /** ????????? */
        private int bauxite;
        /** ??????????????? */
        private int bucket;
        /** ??????????????? */
        private int burner;
        /** ???????????? */
        private int research;

        public Log(String line) {
            try {
                String[] cols = line.split(",", -1);
                SimpleDateFormat format = new SimpleDateFormat(AppConstants.DATE_FORMAT);
                this.date = format.parse(cols[0]);
                this.fuel = Integer.parseInt(cols[1]);
                this.ammo = Integer.parseInt(cols[2]);
                this.metal = Integer.parseInt(cols[3]);
                this.bauxite = Integer.parseInt(cols[4]);
                this.bucket = Integer.parseInt(cols[5]);
                this.burner = Integer.parseInt(cols[6]);
                this.research = Integer.parseInt(cols[7]);
            } catch (ParseException e) {
                LoggerHolder.LOG.warn("????????????????????????????????????????????????????????????:???????????????????????????????????????", e);
                LoggerHolder.LOG.warn(line);
            } catch (NumberFormatException e) {
                LoggerHolder.LOG.warn("????????????????????????????????????????????????????????????:?????????????????????????????????", e);
                LoggerHolder.LOG.warn(line);
            } catch (IndexOutOfBoundsException e) {
                LoggerHolder.LOG.warn("????????????????????????????????????????????????????????????:??????????????????????????????", e);
                LoggerHolder.LOG.warn(line);
            }
        }
    }

    /**
     * ????????????????????????????????????????????????????????????
     *
     */
    private static class DateTimeConverter extends StringConverter<Number> {
        /** ?????????????????????????????????????????? */
        private final long from;
        /** ????????????????????? */
        private final SimpleDateFormat format;

        /**
         * @param from ??????????????????????????????????????????
         */
        public DateTimeConverter(Date from, String format) {
            this.from = from.getTime();
            this.format = new SimpleDateFormat(format);
        }

        @Override
        public Number fromString(String str) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString(Number n) {
            return this.format.format(new Date(n.longValue() + this.from));
        }
    }

    /**
     * ????????????????????????
     *
     */
    private static enum ScaleOption {
        /** 1??? */
        ONE_DAY("1???", "HH:mm", 1, TimeUnit.HOURS.toMillis(2)),
        /** 1?????? */
        ONE_WEEK("1??????", "M???d???", 7, TimeUnit.DAYS.toMillis(1)),
        /** 2?????? */
        TWO_WEEK("2??????", "M???d???", 14, TimeUnit.DAYS.toMillis(1)),
        /** 1?????? */
        ONE_MONTH("1??????", "M???d???", 30, TimeUnit.DAYS.toMillis(2)),
        /** 2?????? */
        TWO_MONTH("2??????", "M???d???", 60, TimeUnit.DAYS.toMillis(5)),
        /** 3?????? */
        THREE_MONTH("3??????", "M???d???", 90, TimeUnit.DAYS.toMillis(10)),
        /** ?????? */
        HALF_YEAR("??????", "M???d???", 180, TimeUnit.DAYS.toMillis(15)),
        /** 1??? */
        ONE_YEAR("1???", "M???d???", 365, TimeUnit.DAYS.toMillis(30));

        private String name;
        private String format;
        private int day;
        private long tickUnit;

        private ScaleOption(String name, String format, int day, long tickUnit) {
            this.name = name;
            this.format = format;
            this.day = day;
            this.tickUnit = tickUnit;
        }

        public String getFormat() {
            return this.format;
        }

        public int getDay() {
            return this.day;
        }

        public long getTickUnit() {
            return this.tickUnit;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
