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

package edu.htl3r.schoolplanner.backend.schoolObjects.lesson.lessonType;

import java.io.Serializable;

import android.graphics.Color;
import edu.htl3r.schoolplanner.R;
import edu.htl3r.schoolplanner.SchoolplannerContext;
import edu.htl3r.schoolplanner.backend.schoolObjects.lesson.LessonType;

public class LessonTypeStandby extends LessonType implements Serializable {
	
	private static final long serialVersionUID = -4941171247877817374L;

	public LessonTypeStandby() {
		fgColor = Color.WHITE;
		bgColor = Color.rgb(80,0,80);
		text = SchoolplannerContext.context.getString(R.string.lessonview_type_standby);
	}
	
}
