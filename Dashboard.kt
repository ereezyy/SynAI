package com.example.synapseai

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun Dashboard() {
    Column {
        Text(text = "Synapse AI Dashboard")
        Text(text = "Upcoming Meetings: [Placeholder]")
        Text(text = "Recent Summaries: [Placeholder]")
        Button(onClick = { /* Implement email drafting logic here */ }) {
            Text("Draft Email")
        }
    }
}
