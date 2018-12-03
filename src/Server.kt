@file:Suppress("UNREACHABLE_CODE")

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.StringBuilder
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.math.abs

const val PORT = 3366

fun main(args : Array<String>) {
    Server(PORT)
}

class Server(port: Int) : ServerSocket(port) {
    var clientList = mutableListOf<ClientHandler>()
    var random = Random()
    init {

        while (true) {
            var socket = accept()
            clientList.add(ClientHandler(socket, this, randomName().toString()))
        }
    }

    private fun randomName(): StringBuilder? {
        var builder = StringBuilder()
        for (i in 1..5) {
            var ch = (abs(random.nextInt()) % 26 + 97)
            builder.append(ch.toChar())
        }
        return builder
    }

    fun sendMessage(msg: String) {
        for (x in clientList) x.write(msg)
    }

    class ClientHandler(private var client: Socket, private var target: Server, var userName: String) : Thread() {
        private var reader: BufferedReader = BufferedReader(InputStreamReader(client.getInputStream()))
        private var writer: PrintWriter = PrintWriter(client.getOutputStream(), true)

        init {
            println("Client $name is connected")
            start()
        }

        fun write(msg: String) {
            writer.println(msg)
        }

        override fun run() {
            super.run()
            try {
                var line = reader.readLine()
                while (line != "good bye") {
                    target.sendMessage("$userName: $line")
                    line = reader.readLine()
                }
                writer.close()
                reader.close()
                client.close()
            } catch (e: Exception) {
                println(e.message)
            }
        }


    }
}