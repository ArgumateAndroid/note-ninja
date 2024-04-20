package com.argumate.noteninja

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.argumate.noteninja.ui.theme.NoteNinjaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewmodel = ViewModelProvider(this).get(HomeViewmodel::class.java)
        setContent {
            NoteNinjaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Home(viewmodel)
                }
            }
        }
    }
}

@Composable
fun Home(viewmodel: HomeViewmodel) {
    Column(modifier = Modifier.padding(16.dp)) {
        BPMSliderComposable(viewmodel.bpm.collectAsState().value, viewmodel.onValueChange)
        ScaleDropDownsComposable(
            viewmodel.startNote.collectAsState().value,
            viewmodel.endNote.collectAsState().value,
            viewmodel.scale.collectAsState().value,
            viewmodel.updateStartNote,
            viewmodel.updateEndNote
        )
        SelectScaleComposable(viewmodel.scale.collectAsState().value, viewmodel.onScaleChange)
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxHeight()
                .padding(bottom = 24.dp)
        ) {
            NoteDisplayComposable(
                viewmodel.key.collectAsState().value,
                viewmodel.scale.collectAsState().value,
                modifier = Modifier.align(
                    Alignment.Center
                )
            )

            PlayButtonComposable(
                viewmodel.updateNote,
                viewmodel.bpm,
                Modifier
                    .align(Alignment.BottomCenter)
            )
        }
    }
}


@Composable
fun SelectScaleComposable(selectedState: Int, onSelectedScaleChange: (Int) -> Unit) {
    val scales = listOf("C..B", "Do..Ti", "Sa..Ni")
    val selectedScale = remember { mutableIntStateOf(selectedState) }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        scales.forEachIndexed { index, scale ->
            Text(
                text = scale,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        selectedScale.intValue = index
                        onSelectedScaleChange(index)
                    }
                    .background(
                        if (selectedScale.intValue == index) Color.Blue else Color.Transparent
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .padding(8.dp),
                color = if (selectedScale.intValue == index) Color.White else Color.Unspecified
            )
        }
    }
}

@Composable
fun PlayButtonComposable(
    updateNote: () -> Unit,
    currentBpm: MutableStateFlow<Float>,
    modifier: Modifier = Modifier
) {
    val isPlaying = remember {
        mutableStateOf(false)
    }
    Button(
        onClick = { isPlaying.value = !isPlaying.value },
        modifier = modifier
    ) {
        Text(text = if (isPlaying.value) "Stop" else "Play")
    }
    LaunchedEffect(isPlaying.value) {
        while (isPlaying.value) {
            delay((60000L / currentBpm.value.toInt()))
            updateNote()
        }
    }
}

@Composable
fun NoteDisplayComposable(text: Int, scale: Int, modifier: Modifier = Modifier) {
    Utils.playMusic(LocalContext.current, getFileName(text))
    Text(
        text = Utils.mapToNote(text, scale),
        modifier = modifier
            .fillMaxWidth(),
        textAlign = TextAlign.Center,
        fontSize = 90.sp
    )
}

fun getFileName(note: Int): String {
    val noteList = Utils.getList(0)
    val noteName = noteList[note % 7].lowercase(Locale.ROOT)
    val noteNumber = note / 7 + 1
    return noteName + noteNumber
}

@Composable
fun ScaleDropDownsComposable(
    startNote: Int,
    endNote: Int,
    scale: Int,
    updateStartNote: (Int) -> Unit,
    updateEndNote: (Int) -> Unit
) {


    val list = remember {
        Utils.getDropDownRange()
    }
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        NoteRangeDropdown(
            startNote = 0,
            currentNote = startNote,
            updateNote = updateStartNote,
            list = list,
            scale = scale
        )
        NoteRangeDropdown(
            startNote = startNote,
            currentNote = endNote,
            scale = scale,
            updateNote = updateEndNote,
            list = list
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteRangeDropdown(
    startNote: Int,
    currentNote: Int,
    scale: Int,
    updateNote: (Int) -> Unit,
    list: List<Int>
) {
    val expanded = remember {
        mutableStateOf(false)
    }
    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = { expanded.value = !expanded.value },

        ) {
        Text(text = Utils.mapToNote(currentNote, scale), modifier = Modifier.menuAnchor())
        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            modifier = Modifier.width(
                120.dp
            )
        ) {
            list.forEach {
                DropdownMenuItem(text = {
                    if (it < startNote)
                        Text(text = Utils.mapToNote(it, scale), color = Color.Gray)
                    else
                        Text(text = Utils.mapToNote(it, scale))
                }, onClick = {
                    if (it >= startNote) {
                        updateNote(it)
                        expanded.value = false
                    }
                })
            }
        }
    }
}

@Composable
fun BPMSliderComposable(value: Float, onValueChange: (Float) -> Unit) {
    Column {
        CurrentBpmComposable(value, onValueChange)
        BPMControlComposable(value, onValueChange)
    }
}

@Composable
fun BPMControlComposable(value: Float, onValueChange: (Float) -> Unit) {
    Slider(value = value, valueRange = 1f..120f, onValueChange = onValueChange)
}

@Composable
fun CurrentBpmComposable(text: Float, updateBpm: (Float) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                .clickable {
                    updateBpm(text - 5)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "-5",
                color = Color.Blue
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                .clickable {
                    updateBpm(text - 1)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "-1",
                color = Color.Blue
            )
        }
        Text(
            text = text.toInt().toString(),
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterVertically),
            fontSize = 24.sp
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                .clickable {
                    updateBpm(text + 1)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+1",
                color = Color.Blue
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                .clickable {
                    updateBpm(text + 5)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+5",
                color = Color.Blue
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NoteNinjaTheme {
//        Home("Android")
    }
}