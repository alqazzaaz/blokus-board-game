package service

import entity.BlokusGame
import service.bot.BotTurnService

/**
 * The root service class is responsible for managing services and the entity layer reference.
 * This class acts as a central hub for every other service within the application.
 */
class RootService{
    val gameService = GameService(this)
    val ioService = IOService(this)
    val networkService = NetworkService(this)
    val playerActionService = PlayerActionService(this)
    val botTurnService = BotTurnService(this)

    var game: BlokusGame? = null
}