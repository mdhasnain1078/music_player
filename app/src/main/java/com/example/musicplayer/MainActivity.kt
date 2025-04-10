package com.example.musicplayer

import com.example.musicplayer.databinding.ActivityMainBinding

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import gaur.himanshu.musicplayerapp.songs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private var service: MusicPlayerService? = null
    private var isBound = false
    private val isPlaying = MutableStateFlow(false) // ✅ Fix added here

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as MusicPlayerService.MusicBinder).getService()
            binder.setMusicList(songs)

            lifecycleScope.launch {
                binder.isPlaying().collectLatest { playing ->
                    isPlaying.value = playing // ✅ Update the state
                }
            }

            lifecycleScope.launch {
                binder.getCurrentTrack().collectLatest { track ->
                    binding.trackName.text = track.name
                    binding.albumArt.setImageResource(track.image)
                }
            }

            lifecycleScope.launch {
                binder.maxDuration().collectLatest { duration ->
                    binding.seekBar.max = duration.toInt()
                    binding.tvTotalTime.text = formatTime(duration.toInt())
                }
            }

            lifecycleScope.launch {
                binder.currentDuration().collectLatest { current ->
                    binding.seekBar.progress = current.toInt()
                    binding.tvCurrentTime.text = formatTime(current.toInt())
                }
            }

            lifecycleScope.launch {
                isPlaying.collectLatest { playing ->
                    binding.btnPlayPause.setImageResource(
                        if (playing) R.drawable.ic_pause else R.drawable.ic_play
                    )
                }
            }

            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPlayPause.setOnClickListener {
            service?.playPause()
        }

        binding.btnPrev.setOnClickListener {
            service?.prev()
        }

        binding.btnNext.setOnClickListener {
            service?.next()
        }

        binding.playButton.setOnClickListener{
            val intent =
                Intent(this@MainActivity, MusicPlayerService::class.java)
            startService(intent)
            bindService(intent, connection, BIND_AUTO_CREATE)
        }

        binding.stopButton.setOnClickListener{
            val intent =
                Intent(this@MainActivity, MusicPlayerService::class.java)
            stopService(intent)
            unbindService(connection)
        }



        val intent = Intent(this, MusicPlayerService::class.java)
        startService(intent)
        bindService(intent, connection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = milliseconds / 1000 / 60
        val seconds = milliseconds / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }




}






//actions = {
//    IconButton(onClick = {
//        val intent =
//            Intent(this@MainActivity, MusicPlayerService::class.java)
//        startService(intent)
//        bindService(intent, connection, BIND_AUTO_CREATE)
//    }) {
//        Icon(
//            imageVector = Icons.Default.PlayArrow,
//            contentDescription = null
//        )
//    }
//
//    IconButton(onClick = {
//        val intent =
//            Intent(this@MainActivity, MusicPlayerService::class.java)
//        stopService(intent)
//        unbindService(connection)
//    }) {
//        Icon(imageVector = Icons.Default.Close, contentDescription = null)
//    }
//
//})
//})