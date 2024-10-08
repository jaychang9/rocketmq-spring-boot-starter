package cn.jaychang.rocketmq.gson;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author ouyang
 * @version 1.0
 **/
public class CustomDateAdapter extends TypeAdapter<Date> {

    private final List<DateFormat> dateFormats = new ArrayList<>();

    public CustomDateAdapter() {
        super();
        addDateFormat(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINESE); // yyyy-MM-dd HH:mm:ss
        addDateFormat(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US); // Nov 2, 2020 10:46:51 AM
        addDateFormat(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault()); // 根据服务器不一样而不一样
    }

    /**
     * Creates a DateFormat with the given time and/or date style in the given
     * locale.
     *
     * @param timeStyle a value from 0 to 3 indicating the time format,
     *                  ignored if flags is 2
     * @param dateStyle a value from 0 to 3 indicating the time format,
     *                  ignored if flags is 1
     * @param aLocale   the locale for the format
     */
    public void addDateFormat(int dateStyle, int timeStyle, Locale aLocale) {
        this.dateFormats.add(DateFormat.getDateTimeInstance(dateStyle, timeStyle, aLocale));
    }

    public void addDateFormat(DateFormat dateFormat) {
        this.dateFormats.add(dateFormat);
    }

    public Date read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        } else {
            return this.deserializeToDate(in.nextString());
        }
    }

    private synchronized Date deserializeToDate(String json) {
        for (DateFormat dateFormat : this.dateFormats) {
            try {
                return dateFormat.parse(json);
            } catch (ParseException ignored) {
            }
        }

        try {
            return ISO8601Utils.parse(json, new ParsePosition(0));
        } catch (ParseException parseException) {
            throw new JsonSyntaxException(json, parseException);
        }
    }

    public synchronized void write(JsonWriter out, Date value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            String dateFormatAsString = this.dateFormats.get(0).format(value);
            out.value(dateFormatAsString);
        }
    }
}
