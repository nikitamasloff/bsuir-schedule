package com.nikitamaslov.bsuirschedule.data.remote

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.nikitamaslov.bsuirschedule.data.model.*
import com.nikitamaslov.bsuirschedule.data.remote.exclude.ExclusionStrategy
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import kotlin.reflect.KClass


@Suppress("UNUSED_PARAMETER")
class Api {

    private companion object Constants {

        private fun urlOfEmployees(): String =
            Uri.parse("https://journal.bsuir.by/api/v1/employees").buildUpon().build().toString()

        private fun urlOfGroups(): String =
            Uri.parse("https://journal.bsuir.by/api/v1/groups").buildUpon().build().toString()

        private fun urlOfEmployeeSchedule(employeeId: Int): String =
            Uri.parse("https://journal.bsuir.by/api/v1/portal/employeeSchedule").buildUpon()
                .appendQueryParameter("employeeId", employeeId.toString()).build().toString()

        /*private fun urlOfGroupSchedule(studentGroup: String): String =
            Uri.parse("https://journal.bsuir.by/api/v1/studentGroup/schedule").buildUpon()
                .appendQueryParameter("studentGroup", studentGroup).build().toString()*/

        private fun urlOfGroupSchedule(groupId: Int): String =
            Uri.parse("https://journal.bsuir.by/api/v1/studentGroup/schedule").buildUpon()
                .appendQueryParameter("id", groupId.toString()).build().toString()

        private fun urlOfAuditory(): String =
            Uri.parse("https://journal.bsuir.by/api/v1/auditory").buildUpon().build().toString()

        //fields
        //private const val examSchedules: String = "examSchedules"
        private const val schedules: String = "schedules"
        private const val schedule: String = "schedule"
        private const val group: String = "studentGroup"
        private const val employee: String = "employee"
        private const val dayOfWeek: String = "weekDay"
        private const val auditoryNumber = "name"
        private const val buildingNumber = "buildingNumber"
        private const val pavilion = "name"

    }

    private val gson: Gson by lazy {
        GsonBuilder()
            .serializeNulls()
            .addDeserializationExclusionStrategy(ExclusionStrategy)
            .setPrettyPrinting()
            .create()
    }

    @JvmName("queryEmployee")
    @Throws(IOException::class)
    fun query(kClass: KClass<Employee>): Array<Employee> {

        val url = urlOfEmployees()

        val bodyArray: JsonArray = try {
            gson.fromJson(urlString(url), JsonArray::class.java)
        } catch (e: Exception) {
            throw IOException()
        }

        if (bodyArray.size() == 0) return emptyArray()

        val resultList = ArrayList<Employee>()
        for (item in bodyArray) {
            val bodyItem: JsonObject = item.asJsonObject
            val teacher: Employee = gson.fromJson(bodyItem, Employee::class.java) ?: continue
            teacher.apply {
                fio = cutTeacherFio(fio)
            }
            if (teacher.id != null)
                resultList.add(teacher)
        }
        println(resultList)
        return resultList.toTypedArray()
    }

    @JvmName("queryGroup")
    @Throws(IOException::class)
    fun query(kClass: KClass<Group>): Array<Group> {

        val url = urlOfGroups()

        val bodyArray: JsonArray = try {
            gson.fromJson(urlString(url), JsonArray::class.java)
        } catch (e: Exception) {
            throw IOException()
        }

        if (bodyArray.size() == 0) return emptyArray()

        val resultList = ArrayList<Group>()
        for (item in bodyArray) {
            val bodyItem: JsonObject = item.asJsonObject
            val group: Group = gson.fromJson(bodyItem, Group::class.java) ?: continue
            if (group.number != null)
                resultList.add(group)
        }
        return resultList.toTypedArray()
    }

    @JvmName("queryEmployeeSchedule")
    @Throws(IOException::class)
    fun query(kClass: KClass<EmployeeSchedule>, employeeId: Int): Array<EmployeeSchedule> {

        val url = urlOfEmployeeSchedule(employeeId)

        val bodyObject: JsonObject = try {
            gson.fromJson(urlString(url), JsonObject::class.java)
        } catch (e: Exception) {
            throw IOException()
        }

        val resultList = ArrayList<EmployeeSchedule>()
        val schedules: JsonArray =
            bodyObject.getAsJsonArray(schedules) ?: return resultList.toTypedArray()
        for (item1 in schedules) {
            val schedulesItem: JsonObject = item1 as JsonObject
            val schedule: JsonArray = schedulesItem.getAsJsonArray(schedule)
            loop@ for (item2 in schedule) {
                val scheduleItem: JsonObject = item2 as JsonObject

                val employee: Employee = gson.fromJson(bodyObject.get(employee), Employee::class.java)
                    ?: return emptyArray()
                val weekDay: String = schedulesItem.get(dayOfWeek).asString ?: continue@loop
                val employeeSchedule: EmployeeSchedule =
                    gson.fromJson(scheduleItem, EmployeeSchedule::class.java)
                        ?: continue@loop
                employeeSchedule.apply {
                    this.employee = employee.apply { fio = cutTeacherFio(fio) }
                    this.dayOfWeek = weekDay
                }
                resultList.add(employeeSchedule)
            }
        }
        return resultList.toTypedArray()
    }

    /*@JvmName("queryGroupSchedule")
    @Throws(IOException::class)
    fun query(kClass: KClass<GroupSchedule>, groupNumber: String): Array<GroupSchedule> {

        val url = urlOfGroupSchedule(groupNumber)

        val bodyObject: JsonObject = try {
            gson.fromJson(urlString(url), JsonObject::class.java)
        } catch (e: Exception) {
            if (isConnectedToInternet()) throw NoInternetConnectionException()
            else throw LoadingException()
        }

        val resultList = ArrayList<GroupSchedule>()
        val schedules: JsonArray =
            bodyObject.getAsJsonArray(schedules) ?: return resultList.toTypedArray()
        for (item1 in schedules) {
            val schedulesItem = item1.asJsonObject
            val schedule: JsonArray = schedulesItem.getAsJsonArray(schedule)
            loop@ for (item2 in schedule) {
                val scheduleItem: JsonObject = item2.asJsonObject

                val studentGroup: Group =
                    gson.fromJson(bodyObject.get(GroupSchedule.SerializedNames.GROUP), Group::class.java)
                        ?: return emptyArray()
                val help = (scheduleItem.getAsJsonArray(GroupSchedule.SerializedNames.EMPLOYEE))
                val employee: Employee? = if (help.size() > 0) gson.fromJson(
                    help[0].asJsonObject,
                    Employee::class.java
                ) else null
                val weekDay: String = schedulesItem.get(GroupSchedule.SerializedNames.DAY_OF_WEEK).asString ?: continue@loop
                val groupSchedule: GroupSchedule =
                    gson.fromJson(scheduleItem, GroupSchedule::class.java)
                        ?: continue@loop
                groupSchedule.apply {
                    this.group = studentGroup
                    this.dayOfWeek = weekDay
                    this.employee = employee?.apply {
                        fio = cutTeacherFio(fio)
                    }
                }
                resultList.add(groupSchedule)
            }
        }
        return resultList.toTypedArray()
    }*/

    @JvmName("queryGroupSchedule")
    @Throws(IOException::class)
    fun query(kClass: KClass<GroupSchedule>, groupId: Int): Array<GroupSchedule> {

        val url = urlOfGroupSchedule(groupId)

        val bodyObject: JsonObject = try {
            gson.fromJson(urlString(url), JsonObject::class.java)
        } catch (e: Exception) {
            throw IOException()
        }

        val resultList = ArrayList<GroupSchedule>()
        val schedules: JsonArray =
            bodyObject.getAsJsonArray(schedules) ?: return resultList.toTypedArray()
        for (item1 in schedules) {
            val schedulesItem = item1.asJsonObject
            val schedule: JsonArray = schedulesItem.getAsJsonArray(schedule)
            loop@ for (item2 in schedule) {
                val scheduleItem: JsonObject = item2.asJsonObject

                val studentGroup: Group =
                    gson.fromJson(bodyObject.get(group), Group::class.java)
                        ?: return emptyArray()
                val help = (scheduleItem.getAsJsonArray(employee))
                val employee: Employee? = if (help.size() > 0) gson.fromJson(
                    help[0].asJsonObject,
                    Employee::class.java
                ) else null
                val weekDay: String = schedulesItem.get(dayOfWeek).asString ?: continue@loop
                val groupSchedule: GroupSchedule =
                    gson.fromJson(scheduleItem, GroupSchedule::class.java)
                        ?: continue@loop
                groupSchedule.apply {
                    this.group = studentGroup
                    this.dayOfWeek = weekDay
                    this.employee = employee?.apply {
                        fio = cutTeacherFio(fio)
                    }
                }
                resultList.add(groupSchedule)
            }
        }
        return resultList.toTypedArray()
    }

    @JvmName("parseAuditory")
    @Throws(IOException::class)
    fun query(kClass: KClass<Auditory>): Array<Auditory> {

        val url = urlOfAuditory()

        val bodyArray: JsonArray = try {
            gson.fromJson(urlString(url), JsonArray::class.java)
        } catch (e: Exception) {
            throw IOException()
        }

        if (bodyArray.size() == 0) return emptyArray()

        val resultList = ArrayList<Auditory>()
        for (item in bodyArray) {
            val bodyItem: JsonObject = item.asJsonObject
            val auditory = Auditory()
            auditory.number = bodyItem.get(auditoryNumber).asString
            val innerObject = bodyItem.getAsJsonObject(buildingNumber)
            auditory.pavilion = innerObject.get(pavilion).asString
            if (auditory.number != null)
                resultList.add(auditory)
        }
        return resultList.toTypedArray()
    }

    private fun cutTeacherFio(fio: String?): String? {
        if (fio == null || fio.isEmpty()) return null
        for (index in (fio.length - 1).downTo(0)) {
            if (fio[index] == '.') {
                if (index + 1 == fio.length) return fio
                return fio.subSequence(0..(index + 1)).toString()
            }
        }
        return fio
    }

    /*
    * Connection util
    * */

    private fun urlString(spec: String): String {
        val url = URL(spec)
        val bufferedReader = BufferedReader(InputStreamReader(url.openStream()))
        val sb = StringBuilder()

        bufferedReader.use { input ->
            var buffer: String? = input.readLine()
            while (buffer != null) {
                sb.append(buffer)
                buffer = input.readLine()
            }
            return sb.toString()
        }
    }

    /*@Throws(IOException::class)
    private fun urlString(urlSpec:String):String {
        return String(getUrlBytes(urlSpec))
    }

    @Throws(IOException::class)
    private fun getUrlBytes(urlSpec:String):ByteArray {
        val url = URL(urlSpec)
        val connection = url.openConnection() as HttpURLConnection
        try
        {
            val output = ByteArrayOutputStream()
            val input = connection.inputStream
            if (connection.responseCode != HttpURLConnection.HTTP_OK)
            {
                throw IOException("Code: " + connection.responseCode + " " + urlSpec)
            }
            val buffer = ByteArray(1024)
            var bytesRead:Int = input.read()
            while (bytesRead > 0)
            {
                bytesRead = input.read(buffer)
                output.write(buffer, 0, bytesRead)
            }
            output.close()
            return output.toByteArray()
        }

        finally
        {
            connection.disconnect()
        }
    }*/

}