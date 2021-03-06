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

package edu.htl3r.schoolplanner.backend.schoolObjects.lesson;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import edu.htl3r.schoolplanner.backend.network.WebUntis;
import edu.htl3r.schoolplanner.backend.schoolObjects.lesson.lessonCode.LessonCodeCancelled;
import edu.htl3r.schoolplanner.backend.schoolObjects.lesson.lessonCode.LessonCodeSubstitute;

public class LessonCodeCreator {

	private Map<String, LessonCode> lessonCodes = new HashMap<String, LessonCode>();
	private LessonCodeSubstitute lessonCodeSubstitude = new LessonCodeSubstitute();
	
	public LessonCodeCreator() {
		lessonCodes.put(WebUntis.CANCELLED, new LessonCodeCancelled());
	}
	
	public LessonCode createLessonCode(String lessonCode) { 
		try {
			return lessonCodes.get(lessonCode) != null ? (LessonCode) lessonCodes.get(lessonCode).clone() : null;
		} catch (CloneNotSupportedException e) {
			Log.w("Misc", "Unable to clone lessonCode",e);
		}
		return lessonCodes.get(lessonCode);
	}
	
	/**
	 * Benoetigt eigene Methode, von Untis kein Code fuer Supplierung vorgegeben.
	 * @return Den LessonCode Supplierung
	 */
	public LessonCodeSubstitute createLessonCodeSubstitude() {
		try {
			return (LessonCodeSubstitute) lessonCodeSubstitude.clone();
		} catch (CloneNotSupportedException e) {
			Log.w("Misc", "Unable to clone lessonCodeSubstitude",e);
		}
		return lessonCodeSubstitude;
	}
	
	public void setLessonCodeColor(String lessonCode, int fgColor, int bgColor) {
		LessonCode lc = lessonCodes.get(lessonCode);
		if(lc != null) {
			lc.setFgColor(fgColor);
			lc.setBgColor(bgColor);
		}
	}
	
	public void setLessonCodeSubstitudeColor(int fgColor, int bgColor) {
		lessonCodeSubstitude.setFgColor(fgColor);
		lessonCodeSubstitude.setBgColor(bgColor);
	}
}
