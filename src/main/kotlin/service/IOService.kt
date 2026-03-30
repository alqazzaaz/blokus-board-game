package service

import entity.BlokusGame
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * The IOService is responsible for saving and loading game states
 * @property rootService A connection back to the headquarters, just in case
 * we need to fetch data from other services to save it
 */
class IOService(private val rootService: RootService) : AbstractRefreshingService()  {

    /**
     * This method saves the current state of the game into a file.
     * @param path The location (file path) where the game should be saved
     */
    fun saveGame(path: String) {
        val game = checkNotNull(rootService.game) { "Kein aktives Spiel" }
        check(!game.isNetwork){ "network game cannot be saved" }
        try {
            val fileOut = FileOutputStream(path)
            val objectOut = ObjectOutputStream(fileOut)
            objectOut.writeObject(game)
            objectOut.close()
            fileOut.close()

            onAllRefreshables { refreshAfterSaveGame() }
        } catch (e: IllegalStateException) {
            println("Fehler beim Speichern ${e.message}")
        }
    }

    /**
     *This method loads a previously saved game from a file
     *@param path The location (file path) where the saved game file is stored
     */
    fun loadGame(path: String) {
        try {
            val fileIn = FileInputStream(path)
            val objectIn = ObjectInputStream(fileIn)
            val game = objectIn.readObject() as BlokusGame
            objectIn.close()
            fileIn.close()

            rootService.game = game
            game.currentGameState.currentParticipantIndex = (game.currentGameState.currentParticipantIndex-1 +
                    game.currentGameState.participantList.size).mod(game.currentGameState.participantList.size)
            onAllRefreshables { refreshAfterInitializeGame() }
            rootService.gameService.nextParticipant()
        } catch (e: IllegalStateException) {
            println("Fehler beim Laden ${e.message}")
        }
    }
}