package com.example.quizconhecimentos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF2F4F8)
                ) {
                    AppQuiz()
                }
            }
        }
    }
}

// Camada conectada ao Banco SQLite
@Composable
fun AppQuiz() {
    val context = LocalContext.current
    val banco = remember { BancoQuizHelper(context) }
    var listaPerguntas by remember { mutableStateOf(banco.buscarPerguntas()) }
    var mostrarCadastro by remember { mutableStateOf(false) }

    if (mostrarCadastro) {
        DialogCadastroPergunta(
            banco = banco,
            onDismiss = { mostrarCadastro = false },
            onSalvo = {
                listaPerguntas = banco.buscarPerguntas()
                mostrarCadastro = false
            }
        )
    }

    if (listaPerguntas.isNotEmpty()) {
        TelaQuiz(
            perguntas = listaPerguntas,
            banco = banco,
            onAbrirCadastro = { mostrarCadastro = true }
        )
    }
}

// Camada Visual (Design de Tela puro)
@Composable
fun TelaQuiz(perguntas: List<Pergunta>, banco: BancoQuizHelper, onAbrirCadastro: () -> Unit) {
    var indiceAtual by remember { mutableStateOf(0) }
    var pontuacao by remember { mutableStateOf(0) }
    var opcaoSelecionada by remember { mutableStateOf<Int?>(null) }

    val quizFinalizado = indiceAtual >= perguntas.size

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Cabeçalho Estilizado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF283593))
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🧠 Quiz Geral",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (quizFinalizado) "Desafio concluído!" else "Pergunta ${indiceAtual + 1} de ${perguntas.size}",
                        fontSize = 14.sp,
                        color = Color(0xFFC5CAE9)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão Nova Pergunta
                    OutlinedButton(
                        onClick = onAbrirCadastro,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = "➕ Nova Pergunta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Selo de Pontuação
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF3949AB)
                    ) {
                        Text(
                            text = "⭐ $pontuacao pts",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 2. Barra de Progresso
        val progresso = if (quizFinalizado) 1f else (indiceAtual.toFloat() / perguntas.size.toFloat())
        LinearProgressIndicator(
            progress = { progresso },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Color(0xFF00C853),
            trackColor = Color(0xFFDDE1E7)
        )

        // 3. Conteúdo Principal (Pergunta ou Tela Final)
        if (quizFinalizado) {
            TelaResultado(
                pontuacao = pontuacao,
                total = perguntas.size,
                banco = banco,
                onReiniciar = {
                    indiceAtual = 0
                    pontuacao = 0
                    opcaoSelecionada = null
                }
            )
        } else {
            val perguntaAtual = perguntas[indiceAtual]
            val letras = listOf("A", "B", "C")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Cartão do Enunciado
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = perguntaAtual.enunciado,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1D21),
                            lineHeight = 28.sp,
                            modifier = Modifier.padding(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Lista de Alternativas
                    perguntaAtual.opcoes.forEachIndexed { index, textoOpcao ->
                        val corFundo = when {
                            opcaoSelecionada == null -> Color.White
                            index == perguntaAtual.respostaCorreta -> Color(0xFFE8F5E9)
                            index == opcaoSelecionada -> Color(0xFFFFEBEE)
                            else -> Color.White
                        }

                        val corBorda = when {
                            opcaoSelecionada == null -> Color(0xFFD0D7DE)
                            index == perguntaAtual.respostaCorreta -> Color(0xFF2E7D32)
                            index == opcaoSelecionada -> Color(0xFFC62828)
                            else -> Color(0xFFD0D7DE)
                        }

                        OutlinedButton(
                            onClick = {
                                if (opcaoSelecionada == null) {
                                    opcaoSelecionada = index
                                    if (index == perguntaAtual.respostaCorreta) {
                                        pontuacao++
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(2.dp, corBorda),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = corFundo)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${letras[index]}) ",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF283593)
                                )
                                Text(
                                    text = textoOpcao,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1A1D21)
                                )
                            }
                        }
                    }
                }

                // Botão para avançar (aparece após responder)
                if (opcaoSelecionada != null) {
                    Button(
                        onClick = {
                            opcaoSelecionada = null
                            indiceAtual++
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283593))
                    ) {
                        Text(
                            text = if (indiceAtual + 1 == perguntas.size) "Ver Resultado Final 🏆" else "Próxima Pergunta ➡️",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TelaResultado(pontuacao: Int, total: Int, banco: BancoQuizHelper, onReiniciar: () -> Unit) {
    var melhorPontuacao by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        banco.salvarRecorde(pontuacao)
        melhorPontuacao = banco.buscarMelhorPontuacao()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🏆", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Fim de Jogo!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1D21)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Você acertou $pontuacao de $total perguntas",
                    fontSize = 18.sp,
                    color = Color(0xFF283593),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Seu Recorde Histórico: $melhorPontuacao pontos",
                    fontSize = 16.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onReiniciar,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text(text = "Jogar Novamente 🔄", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DialogCadastroPergunta(
    banco: BancoQuizHelper,
    onDismiss: () -> Unit,
    onSalvo: () -> Unit
) {
    var enunciado by remember { mutableStateOf("") }
    var opA by remember { mutableStateOf("") }
    var opB by remember { mutableStateOf("") }
    var opC by remember { mutableStateOf("") }
    var correta by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cadastrar Nova Pergunta", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = enunciado,
                    onValueChange = { enunciado = it },
                    label = { Text("Enunciado da Pergunta") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = opA,
                    onValueChange = { opA = it },
                    label = { Text("Opção A") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = opB,
                    onValueChange = { opB = it },
                    label = { Text("Opção B") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = opC,
                    onValueChange = { opC = it },
                    label = { Text("Opção C") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Resposta Correta:", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("A (0)", "B (1)", "C (2)").forEachIndexed { index, label ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = correta == index,
                                onClick = { correta = index }
                            )
                            Text(label, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enunciado.isNotBlank() && opA.isNotBlank() && opB.isNotBlank() && opC.isNotBlank()) {
                        banco.inserirPergunta(enunciado, opA, opB, opC, correta)
                        onSalvo()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF283593))
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
