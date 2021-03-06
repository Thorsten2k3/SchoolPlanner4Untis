/* SchoolPlanner4Untis - Android app to manage your Untis timetable
    Copyright (C) 2011  Mathias Kub <mail@makubi.at>
						Gerald Schreiber <mail@gerald-schreiber.at>
						Philip Woelfel <philip@woelfel.at>
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package edu.htl3r.schoolplanner.backend;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import edu.htl3r.schoolplanner.backend.localdata.LocalData;
import edu.htl3r.schoolplanner.backend.schoolObjects.SchoolHoliday;
import edu.htl3r.schoolplanner.backend.schoolObjects.SchoolTest;
import edu.htl3r.schoolplanner.backend.schoolObjects.SchoolTestType;
import edu.htl3r.schoolplanner.backend.schoolObjects.ViewType;
import edu.htl3r.schoolplanner.backend.schoolObjects.lesson.Lesson;
import edu.htl3r.schoolplanner.backend.schoolObjects.timegrid.Timegrid;
import edu.htl3r.schoolplanner.backend.schoolObjects.viewtypes.SchoolClass;
import edu.htl3r.schoolplanner.backend.schoolObjects.viewtypes.SchoolRoom;
import edu.htl3r.schoolplanner.backend.schoolObjects.viewtypes.SchoolSubject;
import edu.htl3r.schoolplanner.backend.schoolObjects.viewtypes.SchoolTeacher;

/**
 * Interface zum Setzen der Daten.<br>
 * Implementierende Klassen muessen bei Erhalt neuer Daten ueber diese benachrichtig werden, um sie passend verwenden zu koennen.<br>
 * Bekannte Implementierungen: {@link LocalData}, {@link InternalMemory}
 */
public interface DataStore {
	
	/**
	 * Setzt die Schulklassenliste. Wird benoetigt, falls sie z.B. aus dem Netzwerk neu Uebertragen wurden, um sie mit dem lokalen Cache abzugleichen.
	 * @param schoolClasses Neue Liste mit Schulklassen 
	 */
	public void setSchoolClassList(List<SchoolClass> schoolClasses);
	
	
	/**
	 * Setzt die Lehrerliste. Wird benoetigt, falls sie z.B. aus dem Netzwerk neu Uebertragen wurden, um sie mit dem lokalen Cache abzugleichen.
	 * @param schoolTeachers Neue Liste mit Lehrern 
	 */
	public void setSchoolTeacherList(List<SchoolTeacher> schoolTeachers);

	/**
	 * Setzt die Fächerliste. Wird benoetigt, falls sie z.B. aus dem Netzwerk neu Uebertragen wurden, um sie mit dem lokalen Cache abzugleichen.
	 * @param schoolSubjects Neue Liste mit Fächern 
	 */
	public void setSchoolSubjectList(List<SchoolSubject> schoolSubjects);

	/**
	 * Setzt die Raumliste. Wird benoetigt, falls sie z.B. aus dem Netzwerk neu Uebertragen wurden, um sie mit dem lokalen Cache abzugleichen.
	 * @param schoolRooms Neue Liste mit Räumen 
	 */
	public void setSchoolRoomList(List<SchoolRoom> schoolRooms);

	/**
	 * Setzt die Liste von leeren Tagen.
	 * @param holidayList Liste mit freien Tagen
	 */
	public void setSchoolHolidayList(List<SchoolHoliday> holidayList);

	/**
	 * Setzt die vorhandenen Testtypen
	 * @param testTypeList Liste mit TestTypen
	 */
	public void setSchoolTestTypeList(List<SchoolTestType> testTypeList);

	/**
	 * Setzt den Zeitraster.
	 * @param timegrid Zeitraster, der gesetzt werden soll
	 */
	public void setTimegrid(Timegrid timegrid);

	/**
	 * Setzt die Testliste.
	 * @param testList Liste mit Tests
	 */
	public void setSchoolTestList(List<SchoolTest> testList);

	/**
	 * Setzt die Liste mit Stunden.
	 * @param view View, fuer die die Stundenliste gesetzt werden soll
	 * @param lessons Liste von Stunden
	 */
	@Deprecated
	public void setLessons(ViewType view, List<Lesson> lessons);
	
	/**
	 * Setzt mehrere Listen fuer die jeweiligen Daten zwischen Start (inklusive)- und Enddatum (inklusive).<br>
	 * Die Laenge der Liste muss genau die Differenz der Anzahl der Tage zwischen Start- und Enddatum + 1 (da inklusive letzter Tag) sein.<br> 
	 * @param view View, fuer den die Listen gesetzt werden soll
	 * @param startDate Anfangsdatum
	 * @param endDate Enddatum
	 * @param lessonList Liste mit einer Liste mit Stunden pro Tag
	 */
	@Deprecated
	public void setLessons(ViewType view, Calendar startDate, Calendar endDate,
			Map<String, List<Lesson>> lessonList);

	/**
	 * Setzt eine Liste mit Stunden fuer einen bestimmten Tag. Diese Liste ist schon fuer die Anzeige auf der GUI optimiert.
	 * @param view View, fuer den die Stunden gesetzt werden soll
	 * @param date Tag, fuer den die Stunden gesetzt werden sollen
	 * @param lessonList Liste mit Stunden, die gesetzt werden sollen
	 */
	public void setMergedLessons(ViewType view, Calendar date,
			List<Lesson> lessonList);
	
	/**
	 * Setzt mehrere Listen fuer die jeweiligen Daten zwischen Start (inklusive)- und Enddatum (inklusive).<br>
	 * Die Laenge der Liste muss genau die Differenz der Anzahl der Tage zwischen Start- und Enddatum + 1 (da inklusive letzter Tag) sein.<br> 
	 * Diese Liste ist schon fuer die Anzeige auf der GUI optimiert.
	 * @param view View, fuer den die Listen gesetzt werden soll
	 * @param startDate Anfangsdatum
	 * @param endDate Enddatum
	 * @param lessonList Liste mit einer Liste mit Stunden pro Tag
	 */
	public void setMergedLessons(ViewType view, Calendar startDate, Calendar endDate,
			Map<String, List<Lesson>> lessonList);
	
}
