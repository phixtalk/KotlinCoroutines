import kotlinx.coroutines.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//coroutines doesnt block the main thread, unlike Thread.sleep()

fun main(args: Array<String>){
    exampleWithContext()
}

suspend fun printLnDelayed(message: String){
    delay(1000)
    println(message)
}

suspend fun calculateHardThings(startNum: Int): Int {
    delay(1000)
    return startNum * 10
}

fun exampleBlocking() = runBlocking{
    //runblocking runs new coroutine and blocks the current thread until its completion
    //just like Thread.sleep()
    println("one")
    printLnDelayed("two")
    println("three")
}

//Running on another thread but still blocking the main thread
fun examplBlockingDispatcher(){
    runBlocking(Dispatchers.Default) {
        println("one - from thread ${Thread.currentThread().name}")
        printLnDelayed("two - from thread ${Thread.currentThread().name}")
    }
    //outside of runBlocking to show that it's running in the blocked main thread
    println("three - from thread ${Thread.currentThread().name}")
    //it still runs only after the runBlocking is fully executed
}

fun exampleLaunchGlobal() = runBlocking{
    //this doesnt block the main thread, but it also doesnt wait for the coroutine call to execute before ending the program
    //hence the second print statement never executes
    println("one - from thread ${Thread.currentThread().name}")
    GlobalScope.launch {
        printLnDelayed("two - from thread ${Thread.currentThread().name}")
    }
    println("three - from thread ${Thread.currentThread().name}")
}

fun exampleLaunchGlobalWaiting() = runBlocking{
    println("one - from thread ${Thread.currentThread().name}")
    var job = GlobalScope.launch {
        printLnDelayed("two - from thread ${Thread.currentThread().name}")
    }
    println("three - from thread ${Thread.currentThread().name}")
    job.join()//this forces the program to wait for the job to finish before exiting
}

fun exampleLaunchCoroutineScope() = runBlocking{
    //here we dont need to explicity define the job.join method anymore, the program knows to wait for the job to finish
    println("one - from thread ${Thread.currentThread().name}")

    //creating a custom dispatcher
    val customDispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

    this.launch(customDispatcher) {
        printLnDelayed("two - from thread ${Thread.currentThread().name}")
    }
    println("three - from thread ${Thread.currentThread().name}")

    //when using a custom dispatcher, you have to manually shut it down
    (customDispatcher.executor as ExecutorService).shutdown()
}

fun exampleAsynAwait() = runBlocking {
    var startTime = System.currentTimeMillis()

    val deferred1 = async { calculateHardThings(10) }
    val deferred2 = async { calculateHardThings(20) }
    val deferred3 = async { calculateHardThings(30) }

    //here async is returning deferred, so we need to await the results and they run concurrently, and not sequentially
    val sum = deferred1.await() + deferred2.await() + deferred3.await()
    println("async/await result = $sum")

    var endTime = System.currentTimeMillis()
    println("Time taken: ${endTime - startTime}")

}

fun exampleWithContext() = runBlocking {
    var startTime = System.currentTimeMillis()

    val deferred1 = withContext(Dispatchers.Default) { calculateHardThings(10) }
    val deferred2 = withContext(Dispatchers.Default) { calculateHardThings(20) }
    val deferred3 = withContext(Dispatchers.Default) { calculateHardThings(30) }

    //here withContext is returning whatever the function inside it is returning, so we can pass the value directly and use it with having to await
    val sum = deferred1 + deferred2 + deferred3
    println("async/await result = $sum")

    var endTime = System.currentTimeMillis()
    println("Time taken: ${endTime - startTime}")

}

//Summary of this sample is Use async and await when you want to run multiple processes concurrently because it will save a significant amount of time
//However when you have one off operation use withContext