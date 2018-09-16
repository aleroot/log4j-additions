package org.pollerosoftware.log4j.rolling.helper;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * RollingCalendar is a helper class to
 * {@link org.apache.log4j.rolling.TimeBasedRollingPolicy
 * TimeBasedRollingPolicy} or similar timed-based rolling policies. Given a
 * periodicity type and the current time, it computes the start of the next
 * interval.
 *
 * @author Alessio Pollero
 *
 */
public class RollingCalendar extends GregorianCalendar {

	private static final long serialVersionUID = -3940462088728120652L;

    // The gmtTimeZone is used only in computeCheckPeriod() method.
	static final TimeZone GMT_TIMEZONE = TimeZone.getTimeZone("GMT");

	// The code assumes that the following constants are in a increasing sequence.
	public static final int TOP_OF_TROUBLE = -1;
	public static final int TOP_OF_SECOND = 0;
	public static final int TOP_OF_MINUTE = 1;
	public static final int TOP_OF_HOUR = 2;
	public static final int HALF_DAY = 3;
	public static final int TOP_OF_DAY = 4;
	public static final int TOP_OF_WEEK = 5;
	public static final int TOP_OF_MONTH = 6;
	protected int type = TOP_OF_TROUBLE;

	public RollingCalendar() {
		super();
	}

	public RollingCalendar(TimeZone tz, Locale locale) {
		super(tz, locale);
	}

	public void init(String datePattern) {
		type = computeTriggeringPeriod(datePattern);
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getNextCheckMillis(Date now) {
		return getNextCheckDate(now).getTime();
	}


	/**
	 * This method computes the roll over period by looping over the
	 * periods, starting with the shortest, and stopping when the r0 is
	 * different from from r1, where r0 is the epoch formatted according
	 * the datePattern (supplied by the user) and r1 is the
	 * epoch+nextMillis(i) formatted according to datePattern. 
	 * @param datePattern the patter of the date
	 * @return integer representing one of the constants of this class, default: TOP_OF_TROUBLE
	 */
	public int computeTriggeringPeriod(String datePattern) {
		RollingCalendar rollingCalendar = new RollingCalendar(GMT_TIMEZONE, Locale.getDefault());

		// set sate to 1970-01-01 00:00:00 GMT
		Date epoch = new Date(0);

		if (datePattern != null) {
			for (int i = TOP_OF_SECOND; i <= TOP_OF_MONTH; i++) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
				simpleDateFormat.setTimeZone(GMT_TIMEZONE); // do all date formatting in GMT

				String r0 = simpleDateFormat.format(epoch);
				rollingCalendar.setType(i);

				Date next = new Date(rollingCalendar.getNextCheckMillis(epoch));
				String r1 = simpleDateFormat.format(next);

				if ((r0 != null) && (r1 != null) && !r0.equals(r1)) {
					return i;
				}
			}
		}

		return TOP_OF_TROUBLE; // Deliberately head for trouble...
	}

	public Date getNextCheckDate(Date now) {
		this.setTime(now);

		switch (type) {
		case TOP_OF_SECOND:
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.SECOND, 1);

			break;

		case TOP_OF_MINUTE:
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.MINUTE, 1);

			break;

		case TOP_OF_HOUR:
			this.set(Calendar.MINUTE, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.HOUR_OF_DAY, 1);

			break;

		case HALF_DAY:
			this.set(Calendar.MINUTE, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);

			int hour = get(Calendar.HOUR_OF_DAY);

			if (hour < 12) {
				this.set(Calendar.HOUR_OF_DAY, 12);
			} else {
				this.set(Calendar.HOUR_OF_DAY, 0);
				this.add(Calendar.DAY_OF_MONTH, 1);
			}

			break;

		case TOP_OF_DAY:
			this.set(Calendar.HOUR_OF_DAY, 0);
			this.set(Calendar.MINUTE, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.DATE, 1);

			break;

		case TOP_OF_WEEK:
			this.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
			this.set(Calendar.HOUR_OF_DAY, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.WEEK_OF_YEAR, 1);

			break;

		case TOP_OF_MONTH:
			this.set(Calendar.DATE, 1);
			this.set(Calendar.HOUR_OF_DAY, 0);
			this.set(Calendar.SECOND, 0);
			this.set(Calendar.MILLISECOND, 0);
			this.add(Calendar.MONTH, 1);

			break;

		default:
			throw new IllegalStateException("Unknown periodicity type.");
		}

		return getTime();
	}
}
