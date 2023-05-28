package fastcampus.aop.part4.githubrepositoryproject

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

class CoroutinesTest01 {
    @Test
    fun test01() = runBlocking {
        val time = measureTimeMillis {
            val name = getFirstName()
            val lastName = getLastName()
            println("Hello, $name $lastName")
        }
        println("measure Time : $time ")
    }

    @Test
    fun test02 ()= runBlocking {
        val time = measureTimeMillis {
            val name = async {getFirstName()  }
            val lastName = async { getLastName() }
            println("Hello, ${name.await()} $lastName.await()")
        }
        println("measure Time : $time ")
    }
    suspend fun getFirstName():String{
        delay(1000)
        return "이"
    }
    suspend fun getLastName():String{
        delay(1000)
        return "기정"
    }
}