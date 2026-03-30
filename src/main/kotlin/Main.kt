import gui.BlokusApplication

/**
 * Main entry point that starts the [BlokusApplication]
 *
 * Once the application is closed, it prints a message indicating the end of the application.
 */
fun main() {
    BlokusApplication().show()
    println("Application ended. Goodbye")
}