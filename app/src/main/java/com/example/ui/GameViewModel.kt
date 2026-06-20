package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.BikeEntity
import com.example.data.database.TrackEntity
import com.example.data.database.UserProfile
import com.example.data.repository.GameRepository
import com.example.game.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class GameViewModel(
    application: Application,
    private val repository: GameRepository
) : AndroidViewModel(application) {

    // 1. Data mapping from DB
    val userProfileState: StateFlow<UserProfile?> = repository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val bikesState: StateFlow<List<BikeEntity>> = repository.allBikesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tracksState: StateFlow<List<TrackEntity>> = repository.allTracksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Active Visual Screen State
    // Menu states: "MainMenu", "Garage", "TrackSelect", "GamePlay", "Settings"
    private val _currentScreen = MutableStateFlow("MainMenu")
    val currentScreen = _currentScreen.asStateFlow()

    // 3. Active Gameplay Loop variables
    private val _bikeState = MutableStateFlow(BikeState())
    val bikeState = _bikeState.asStateFlow()

    private val _coinsInSession = MutableStateFlow(0)
    val coinsInSession = _coinsInSession.asStateFlow()

    private val _flipsInSession = MutableStateFlow(0)
    val flipsInSession = _flipsInSession.asStateFlow()

    private val _activeTrack = MutableStateFlow<TrackInfo?>(null)
    val activeTrack = _activeTrack.asStateFlow()

    private val _activeBike = MutableStateFlow<BikeEntity?>(null)
    val activeBike = _activeBike.asStateFlow()

    private val _gameState = MutableStateFlow<PlayState>(PlayState.COUNTDOWN)
    val gameState = _gameState.asStateFlow()

    private val _countdownVal = MutableStateFlow(3)
    val countdownVal = _countdownVal.asStateFlow()

    private val _raceTimeMillis = MutableStateFlow(0L)
    val raceTimeMillis = _raceTimeMillis.asStateFlow()

    private val _coinsOnTrack = MutableStateFlow<List<GameCoin>>(emptyList())
    val coinsOnTrack = _coinsOnTrack.asStateFlow()

    private val _boostPads = MutableStateFlow<List<BoostPad>>(emptyList())
    val boostPads = _boostPads.asStateFlow()

    private val _activeDecos = MutableStateFlow<List<EnvironmentalDeco>>(emptyList())
    val activeDecos = _activeDecos.asStateFlow()

    // Transient UI Notification (e.g. "+25 Backflip!")
    private val _trickNotification = MutableStateFlow<String?>(null)
    val trickNotification = _trickNotification.asStateFlow()

    private val _isBoosting = MutableStateFlow(false)
    val isBoosting = _isBoosting.asStateFlow()

    // Control Inputs
    var isThrottlePressed = false
    var isBrakePressed = false
    var isTiltForwardPressed = false
    var isTiltBackPressed = false

    private var gameLoopJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
        if (screen != "GamePlay") {
            stopGameLoop()
        }
    }

    // 4. Shop / Upgrades actions
    fun purchaseBike(bikeId: String, cost: Int) {
        viewModelScope.launch {
            repository.unlockBike(bikeId, cost)
        }
    }

    fun purchaseTrack(trackId: String, cost: Int) {
        viewModelScope.launch {
            repository.unlockTrack(trackId, cost)
        }
    }

    fun upgradeAttribute(bikeId: String, attribute: String, cost: Int) {
        viewModelScope.launch {
            repository.upgradeBikeAttribute(bikeId, attribute, cost)
        }
    }

    fun customizeColors(bikeId: String, primary: String, secondary: String) {
        viewModelScope.launch {
            repository.updateBikeColors(bikeId, primary, secondary)
        }
    }

    fun addCheatCoins() {
        viewModelScope.launch {
            repository.addCoins(500)
        }
    }

    fun selectBike(bikeId: String) {
        viewModelScope.launch {
            repository.selectBike(bikeId)
        }
    }

    fun selectTrack(trackId: String) {
        viewModelScope.launch {
            repository.selectTrack(trackId)
        }
    }

    // 5. Game session lifecycle
    fun startGame(trackId: String, bikeId: String) {
        val tracksList = TrackData.TRACKS
        val track = tracksList.find { it.id == trackId } ?: tracksList[0]
        val bike = bikesState.value.find { it.bikeId == bikeId } ?: BikeEntity(
            bikeId = "scout", isUnlocked = true, primaryColorHex = "#E11D48", secondaryColorHex = "#1E293B"
        )

        _activeTrack.value = track
        _activeBike.value = bike
        _bikeState.value = BikeState(x = 60f, y = TrackData.getTrackHeight(track.id, 60f) - 30f)
        _coinsInSession.value = 0
        _flipsInSession.value = 0
        _raceTimeMillis.value = 0L
        _trickNotification.value = null
        _isBoosting.value = false

        // Generate dynamic items
        _coinsOnTrack.value = TrackData.generateCoinsForTrack(track.id, track.length)
        _boostPads.value = TrackData.generateBoostPadsForTrack(track.id, track.length)
        _activeDecos.value = TrackData.generateDecosForTrack(track.id, track.length)

        _gameState.value = PlayState.COUNTDOWN
        navigateTo("GamePlay")

        // Input resets
        isThrottlePressed = false
        isBrakePressed = false
        isTiltForwardPressed = false
        isTiltBackPressed = false

        // Start countdown trigger
        viewModelScope.launch {
            for (i in 3 downTo 1) {
                _countdownVal.value = i
                delay(750)
            }
            _gameState.value = PlayState.RUNNING
            startGameLoop()
        }
    }

    private fun startGameLoop() {
        stopGameLoop()
        gameLoopJob = viewModelScope.launch {
            val tickRateMs = 16L // ~60 FPS
            var lastTick = System.currentTimeMillis()

            while (isActive && _gameState.value == PlayState.RUNNING) {
                val cycleStart = System.currentTimeMillis()
                val delta = cycleStart - lastTick
                lastTick = cycleStart

                // Update timer
                _raceTimeMillis.value += delta

                val currentBike = _activeBike.value ?: continue
                val currentTrack = _activeTrack.value ?: continue

                // 1. Calculate physics step
                val oldState = _bikeState.value
                val newState = BikePhysics.step(
                    currentState = oldState,
                    throttle = isThrottlePressed,
                    brake = isBrakePressed,
                    tiltForward = isTiltForwardPressed,
                    tiltBack = isTiltBackPressed,
                    engineLevel = currentBike.engineLevel,
                    suspensionLevel = currentBike.suspensionLevel,
                    tiresLevel = currentBike.tiresLevel,
                    weightLevel = currentBike.weightLevel,
                    track = currentTrack
                )

                // 2. Check crash state
                if (newState.isCrashed) {
                    _gameState.value = PlayState.CRASHED
                    stopGameLoop()
                    _bikeState.value = newState
                    break
                }

                // 3. Flip triggered notification
                if (newState.flipBonusAwarded > oldState.flipBonusAwarded) {
                    _flipsInSession.value = newState.flipBonusAwarded
                    val coinsWon = 25
                    _coinsInSession.value += coinsWon
                    showTrickNotification("AIR FLIP BONUS +$coinsWon 🪙!")
                }

                // 4. Boost pad collisions
                var isCurrentlyBoosting = false
                _boostPads.value.forEach { pad ->
                    if (newState.isGrounded && newState.x >= pad.x && newState.x <= pad.x + pad.width + 10f) {
                        isCurrentlyBoosting = true
                    }
                }
                
                var adjustedState = newState
                if (isCurrentlyBoosting) {
                    _isBoosting.value = true
                    // Give horizontal boost
                    adjustedState = newState.copy(
                        vx = minOf(newState.vx + 0.65f, (14f + (currentBike.engineLevel - 1) * 2.2f) * 1.35f)
                    )
                } else {
                    _isBoosting.value = false
                }

                // 5. Coin collection checking
                val remainingCoins = _coinsOnTrack.value.toMutableList()
                var collectedInTick = 0
                for (i in remainingCoins.indices) {
                    val coin = remainingCoins[i]
                    if (!coin.isCollected) {
                        val dx = adjustedState.x - coin.x
                        val dy = adjustedState.y - coin.y
                        val dist = sqrt(dx * dx + dy * dy.toDouble()).toFloat()
                        // Coin collection bubble radius
                        if (dist < 34f) {
                            coin.isCollected = true
                            collectedInTick++
                        }
                    }
                }
                if (collectedInTick > 0) {
                    _coinsInSession.value += collectedInTick * 10 // Each coin 10 credits
                    _coinsOnTrack.value = remainingCoins
                }

                // 6. Check Victory goal line
                if (adjustedState.x >= currentTrack.length) {
                    _gameState.value = PlayState.VICTORY
                    stopGameLoop()
                    
                    // Save best scores and award coins
                    viewModelScope.launch {
                        val sessionCoins = _coinsInSession.value
                        repository.addCoins(sessionCoins + 100) // Victory Flat Reward: +100
                        repository.saveBestTime(currentTrack.id, _raceTimeMillis.value)
                    }
                }

                _bikeState.value = adjustedState

                // Sleep remainder
                val duration = System.currentTimeMillis() - cycleStart
                val remainder = tickRateMs - duration
                if (remainder > 0) {
                    delay(remainder)
                } else {
                    delay(1) // Yield thread
                }
            }
        }
    }

    private fun showTrickNotification(msg: String) {
        _trickNotification.value = msg
        viewModelScope.launch {
            delay(1500)
            if (_trickNotification.value == msg) {
                _trickNotification.value = null
            }
        }
    }

    fun restartGame() {
        val track = _activeTrack.value ?: return
        val bike = _activeBike.value ?: return
        startGame(track.id, bike.bikeId)
    }

    fun pauseGame() {
        if (_gameState.value == PlayState.RUNNING) {
            _gameState.value = PlayState.PAUSED
            stopGameLoop()
        }
    }

    fun resumeGame() {
        if (_gameState.value == PlayState.PAUSED) {
            _gameState.value = PlayState.RUNNING
            startGameLoop()
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
    }
}

enum class PlayState {
    COUNTDOWN, RUNNING, PAUSED, CRASHED, VICTORY
}

class GameViewModelFactory(
    private val application: Application,
    private val repository: GameRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
