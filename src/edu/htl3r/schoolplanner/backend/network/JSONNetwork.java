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

package edu.htl3r.schoolplanner.backend.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;
import edu.htl3r.schoolplanner.CalendarUtils;
import edu.htl3r.schoolplanner.backend.DataProvider;
import edu.htl3r.schoolplanner.backend.Preferences;
import edu.htl3r.schoolplanner.backend.schoolObjects.SchoolHoliday;
import edu.htl3r.schoolplanner.backend.schoolObjects.SchoolObject;
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
 * Netzwerkzugriff fuer die Datenabfrage ueber JSON.
 */
public class JSONNetwork implements DataProvider{

	private final String jsonrpcVersion = "2.0";

	private final NetworkAccess network = new Network();

	private final JSONParser jsonParser = new JSONParser();
	private final LessonProcessor lessonProcessor = new LessonProcessor();

	private String username;
	private String password;
	
	private long latestTimetableImportTime = 0;
	
	private Map<Class<? extends ViewType>, Integer> viewTypeMapping = new HashMap<Class<? extends ViewType>, Integer>();
	
	public JSONNetwork() {
		viewTypeMapping.put(SchoolClass.class, WebUntis.SCHOOLCLASS);
		viewTypeMapping.put(SchoolTeacher.class, WebUntis.SCHOOLTEACHER);
		viewTypeMapping.put(SchoolSubject.class, WebUntis.SCHOOLSUBJECT);
		viewTypeMapping.put(SchoolRoom.class, WebUntis.SCHOOLROOM);
	}

	public void setPreferences(Preferences preferences) {
		network.setServerUrl(preferences.getServerUrl());
		network.setSchool(preferences.getSchool());
		this.username = preferences.getUsername();
		this.password = preferences.getPassword();
	}

	/**
	 * Liefert die Daten zu einer JSON-Anfrage.
	 * 
	 * @param request
	 *            JSON-Anfrage
	 * @return Antwort auf die Anfrage
	 * @throws JSONException
	 * @throws IOException 
	 */
	private JSONObject getJSONData(final JSONObject request) throws IOException {
		Log.d("METHOD_CALL", "JSONNetwork.getJSONData(" + request
				+ " : String)");
		JSONObject response = null;
		String responseString = network.getResponse(request.toString());
		
		try {
			response = parseData(responseString);
		

		if(response.has("error")) {
			JSONObject errorObject = response.getJSONObject("error");
			int errorCode = errorObject.getInt("code");
			String errorMessage = errorObject.getString("message");
			Log.d("JSON", "Received error code: "+errorCode+ ", message: "+errorMessage);
			
			if(errorCode == 0 && errorMessage.equals("not authenticated")) {
				Log.d("Network", "Reauthenticating");
				if(authenticate()) {
					responseString = network.getResponse(request.toString());
					response = parseData(responseString);
				}
			}
			else {
				throw new IOException("Received error code: +"+errorCode+ ", message: "+errorMessage);
			}
		
		}
		
		} catch (JSONException e) {
			Log.w("JSON", "Unable to parse String to JSONObject",e);
		}
		
		return response;
	}

	private JSONObject requestList(String id, String method)
			throws JSONException, IOException {
		Log.d("METHOD_CALL", "JSONNetwork.requestList(" + id + " : String, "
				+ method + " : String)");

		final JSONObject request = new JSONObject();

		try {
			request.put("jsonrpc", jsonrpcVersion);
			request.put("method", method);
			request.put("id", id);
			// TODO: Zur Zeit unterstuetzt der Server das Weglassen der params nicht.
			request.put("params", "");
		} catch (JSONException e) {
			Log.e("JSON", "Error on building request for list",e);
		}
		return getJSONData(request);
	}

	/**
	 * Wandelt den uebergebenen String, falls moeglich, in ein JSONObject um.
	 * Sollte dies Fehlschlagen (z.B. weil der String nicht JSON-Konform ist),
	 * wird eine JSONException geworfen
	 * 
	 * @param data
	 *            String, der geparst werden soll
	 * @return JSONObject, das die Daten des uebergebenen Strings repraesentiert
	 * @throws JSONException
	 *             Wird geworfen, falls es nicht moeglich ist, den String zu
	 *             parsen
	 */
	private JSONObject parseData(String data) throws JSONException {
		JSONTokener t = new JSONTokener(data);
		Object next = t.nextValue();

		Log.d("JSON", "Got class: " + next.getClass());
		Log.d("JSON", "Got value: " + next.toString());
		Log.d("JSON", "Another object available: " + t.more());

		if (next instanceof JSONObject) {
			return (JSONObject) next;
		}
		else {
			// Kann bei falscher URL- oder Schulangabe passieren.
			throw new JSONException("Unable to parse data");
		}
		
	}

	private List<? extends ViewType> getViewTypeList(String id, String method) throws IOException {
		List<? extends ViewType> responseList = null;

		try {
			JSONObject responseObject = requestList(id, method);

			JSONArray result = responseObject.getJSONArray("result");
			Log.v("JSON", "Got object type [response/result]: "
					+ responseObject.get("result").getClass().toString());
			Log.v("JSON",
					"Got object data [response/result]: " + result.toString());

			if (method.equals(JSONGetMethods.getTeachers)) {
				responseList = jsonParser.jsonToTeacherList(result);
			} else if (method.equals(JSONGetMethods.getClasses)) {
				responseList = jsonParser.jsonToClassList(result);
			} else if (method.equals(JSONGetMethods.getSubjects)) {
				responseList = jsonParser.jsonToSubjectList(result);
			} else if (method.equals(JSONGetMethods.getRooms)) {
				responseList = jsonParser.jsonToRoomList(result);
			} else {
				Log.e("JSON", "Unknown request method: " + method);
			}

		} catch (JSONException e) {
			Log.e("JSON", "Unable to parse JSON-String", e);
		}
		if(responseList == null) {
			throw new IOException("Empty list returned, id:"+id+", method:"+method);
		}
		return responseList;
	}

	private List<SchoolObject> getList(String id, String method) throws IOException {
		List<SchoolObject> responseList = null;

		try {
			JSONObject responseObject = requestList(id, method);

			JSONArray result = responseObject.getJSONArray("result");
			Log.v("JSON", "Got object type [response/result]: "
					+ responseObject.get("result").getClass().toString());
			Log.v("JSON",
					"Got object data [response/result]: " + result.toString());

			if (method.equals(JSONGetMethods.getHolidays)) {
				responseList = jsonParser.jsonToHolidayList(result);
			} else {
				Log.e("JSON", "Unknown request method: " + method);
			}

		} catch (JSONException e) {
			Log.e("JSON", "Unable to parse JSON-String", e);
		}
		return responseList;
	}
	
	private SchoolObject getSchoolObject(String id, String method) throws IOException {
		SchoolObject response = null;

		try {
			JSONObject responseObject = requestList(id, method);

			JSONArray result = responseObject.getJSONArray("result");
			Log.v("JSON", "Got object type [response/result]: "
					+ responseObject.get("result").getClass().toString());
			Log.v("JSON",
					"Got object data [response/result]: " + result.toString());

			if (method.equals(JSONGetMethods.getTimegridUnits)) {
				response = jsonParser.jsonToTimegrid(result);
			} else {
				Log.e("JSON", "Unknown request method: " + method);
			}

		} catch (JSONException e) {
			Log.e("JSON", "Unable to parse JSON-String", e);
		}
		return response;
	}

	@Override
	public List<SchoolTeacher> getSchoolTeacherList() throws IOException {
		final String id = "ID";
		final String method = JSONGetMethods.getTeachers;

		List<SchoolTeacher> list = new ArrayList<SchoolTeacher>();
		for (ViewType obj : getViewTypeList(id, method)) {
			if (obj instanceof SchoolTeacher) {
				list.add((SchoolTeacher) obj);
			}
		}
		return list;
	}

	@Override
	public List<SchoolClass> getSchoolClassList() throws IOException {
		final String id = "ID";
		final String method = JSONGetMethods.getClasses;

		List<SchoolClass> list = new ArrayList<SchoolClass>();
		for (ViewType obj : getViewTypeList(id, method)) {
			if (obj instanceof SchoolClass) {
				list.add((SchoolClass) obj);
			}
		}
		return list;
	}

	@Override
	public List<SchoolSubject> getSchoolSubjectList() throws IOException {
		final String id = "ID";
		final String method = JSONGetMethods.getSubjects;

		List<SchoolSubject> list = new ArrayList<SchoolSubject>();
		for (ViewType obj : getViewTypeList(id, method)) {
			if (obj instanceof SchoolSubject) {
				list.add((SchoolSubject) obj);
			}
		}
		return list;
	}

	@Override
	public List<SchoolRoom> getSchoolRoomList() throws IOException {
		final String id = "ID";
		final String method = JSONGetMethods.getRooms;

		List<SchoolRoom> list = new ArrayList<SchoolRoom>();
		for (ViewType obj : getViewTypeList(id, method)) {
			if (obj instanceof SchoolRoom) {
				list.add((SchoolRoom) obj);
			}
		}
		return list;
	}

	@Override
	public List<SchoolHoliday> getSchoolHolidayList() throws IOException {
		final String id = "ID";
		final String method = JSONGetMethods.getHolidays;

		List<SchoolHoliday> list = new ArrayList<SchoolHoliday>();
		for (SchoolObject obj : getList(id, method)) {
			if (obj instanceof SchoolHoliday) {
				list.add((SchoolHoliday) obj);
			}
		}
		return list;
	}

	@Override
	public Timegrid getTimegrid() throws IOException {
		final String id = "ID";
		final String method = JSONGetMethods.getTimegridUnits;
		
		Timegrid timegrid = new Timegrid();
		SchoolObject obj = getSchoolObject(id, method);
		if (obj instanceof Timegrid) {
			timegrid = (Timegrid) obj;
		}
		return timegrid;
	}

	@Override
	public Map<String, List<Lesson>> getLessons(ViewType view, Calendar startDate,
			Calendar endDate) throws IOException {
		//Log.d("METHOD_CALL", "JSONNetwork.getLessons(" + type.getSimpleName() + " : Class<? extends ViewType>, "+value+" : String, "+date.get(Calendar.YEAR)+"-"+date.get(Calendar.MONTH)+"-"+date.get(Calendar.DAY_OF_MONTH)+" "+date.get(Calendar.HOUR_OF_DAY)+":"+date.get(Calendar.MINUTE)+" : Date");
		long ms_methodstart = System.currentTimeMillis();
		final String id = "ID";
		final String method = JSONGetMethods.getTimetable;
		
		final JSONObject request = new JSONObject();
		final JSONObject params = new JSONObject();
		
		Map<String, List<Lesson>> responseList = new HashMap<String, List<Lesson>>();
		
		try {
			params.put("id", view.getId());
			params.put("type", viewTypeMapping.get(view.getClass()));
			
			String startYear = ""+startDate.get(Calendar.YEAR);
			// Intern 0 - 11
			String startMonth = ""+(startDate.get(Calendar.MONTH)+1);
			String startDay = ""+startDate.get(Calendar.DAY_OF_MONTH);
			
			if(startMonth.length() < 2) {
				startMonth = "0"+startMonth;
			}
			
			if(startDay.length() < 2) {
				startDay = "0"+startDay;
			}
			
			String endYear = ""+endDate.get(Calendar.YEAR);
			// Intern 0 - 11
			String endMonth = ""+(endDate.get(Calendar.MONTH)+1);
			String endDay = ""+endDate.get(Calendar.DAY_OF_MONTH);
			
			if(endMonth.length() < 2) {
				endMonth = "0"+endMonth;
			}
			
			if(endDay.length() < 2) {
				endDay = "0"+endDay;
			}
			
			params.put("startDate",startYear+startMonth+startDay);
			params.put("endDate",endYear+endMonth+endDay);
			
			request.put("jsonrpc", jsonrpcVersion);
			request.put("method", method);
			request.put("id", id);
			request.put("params", params);
			long ms_objectcreated = System.currentTimeMillis();
			JSONObject response = getJSONData(request);
			long ms_responsegot = System.currentTimeMillis();
			JSONArray result = response.getJSONArray("result");
			
			responseList = jsonParser.jsonToLessonMap(result);
			lessonProcessor.addEmptyDaysToLessonMap(responseList, startDate, endDate);
			
			long ms_responseparsed = System.currentTimeMillis();
			Log.d("SPEEDTESTING", getClass().getSimpleName() + ": create JSON: " +(ms_objectcreated-ms_methodstart) +" ms");
			Log.d("SPEEDTESTING", getClass().getSimpleName() + ": get response: " +(ms_responsegot-ms_objectcreated) +" ms");
			Log.d("SPEEDTESTING", getClass().getSimpleName() + ": parse JSON response: " +(ms_responseparsed-ms_responsegot) +" ms");
			
		} catch (JSONException e) {
			Log.e("JSON", "Error on requesting lessons",e);
		}
		
		return responseList;
	}

	@Override
	public List<Lesson> getLessons(ViewType type,
			Calendar date) throws IOException {
		List<Lesson> result = getLessons(type, date, date).get(CalendarUtils.getCalendarAs8601String(date));
		return result != null ? result : new ArrayList<Lesson>();
	}
	
	@Override
	public List<SchoolTestType> getSchoolTestTypeList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SchoolTest> getSchoolTestList() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Versucht, eine Verbindung zum Server herzustellen und sich mit diesem zu
	 * authentifizieren. Ist dies erfolgreich, wird die SessionID gesetzt, mit
	 * der jede weitere Abfrage durchgefuehrt wird.
	 * 
	 * @return true, wenn die Authentifizierung erfolgreich war
	 * @throws IOException 
	 */
	public boolean authenticate() throws IOException {
		Log.d("METHOD_CALL", "JSONNetwork.authenticate()");

		final String id = "ID";
		final String method = "authenticate";
		final JSONObject params = new JSONObject();
		final JSONObject request = new JSONObject();

		JSONObject response = null;

		try {
			params.put("user", this.username);
			params.put("password", this.password);

			request.put("jsonrpc", jsonrpcVersion);
			request.put("method", method);
			request.put("params", params);
			request.put("id", id);

			response = getJSONData(request);
		
			// TODO: Pruefung auf ID und jsonrpc-version?
			if(response != null) {
				JSONObject result = response.getJSONObject("result");
				if(result != null) {
					String sessionId = result.getString("sessionId");
					if(!result.equals("null")) {
						network.setJsessionid(sessionId);
						return true;
					}
					else {
						network.setJsessionid(null);
					}
				}
			}
		} catch (JSONException e) {
			Log.e("JSON", "Error on authentication", e);
		}
		return false;
	}

	@Override
	public List<Lesson> getMergedLessons(ViewType view, Calendar date)
			throws IOException {
		List<Lesson> lessonList = getLessons(view, date);
		return lessonList != null ? lessonProcessor.mergeLessons(lessonList) : null;
	}

	@Override
	public Map<String, List<Lesson>> getMergedLessons(ViewType view,
			Calendar startDate, Calendar endDate) throws IOException {
		Map<String, List<Lesson>> lessonMap = getLessons(view, startDate, endDate);
		return lessonMap != null ? lessonProcessor.mergeLessons(lessonMap) : null;
	}
	
	@Override
	public List<SchoolTest> getSchoolTestList(ViewType view, Calendar startDate,
			Calendar endDate) {
		// TODO Not implemented in v1.0
		return null;
	}

	@Override
	public void saveSchoolTest(SchoolTest schoolTest) {
		// TODO Not implemented in v1.0
	}
	
	public void initStatusData() throws IOException {		
		final String id = "ID";
		final String method = JSONGetMethods.getStatusData;
		
		JSONObject response;
		try {
			response = requestList(id, method);
			JSONObject result = response.getJSONObject("result");
			jsonParser.resyncStatusData(result);
		} catch (JSONException e) {
			Log.e("JSON", "Unable to parse JSON-String", e);
		}
	}
	
	private long getLatestTimetableImportTime() throws IOException {
		final String id = "ID";
		final String method = JSONGetMethods.getLatestImportTime;
		
		long latestImport = -1;
		
		try {
			JSONObject response = requestList(id, method);
			latestImport = response.getLong("result");
			Log.v("Misc","Last import time: "+response.get("result"));	
		} catch (JSONException e) {
			Log.e("JSON", "Unable to parse JSON-String", e);
		}
		
		return latestImport >= 0 ? latestImport : latestTimetableImportTime;
	}
	
	public boolean timetableUpdated() throws IOException {
		long newLatestTimetableImportTime = getLatestTimetableImportTime();
		
		if(newLatestTimetableImportTime > latestTimetableImportTime) {
			latestTimetableImportTime = newLatestTimetableImportTime;
			return true;
		}
		
		return false;
	}

}
