package com.example.quizconhecimentos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Modelo de dados que representa uma pergunta do Quiz
data class Pergunta(
    val id: Int,
    val enunciado: String,
    val opcoes: List<String>,
    val respostaCorreta: Int // Índice da resposta certa: 0 (A), 1 (B) ou 2 (C)
)

class BancoQuizHelper(context: Context) : SQLiteOpenHelper(
    context,
    "quiz_gerais.db", // Nome do arquivo do banco no dispositivo
    null,
    2 // Versão do banco de dados
) {
    override fun onCreate(db: SQLiteDatabase) {
        val sqlCriarTabela = """
            CREATE TABLE perguntas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                enunciado TEXT NOT NULL,
                opcao_a TEXT NOT NULL,
                opcao_b TEXT NOT NULL,
                opcao_c TEXT NOT NULL,
                correta INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(sqlCriarTabela)
        db.execSQL("CREATE TABLE recordes (id INTEGER PRIMARY KEY AUTOINCREMENT, pontos INTEGER NOT NULL)")

        // Carga inicial de perguntas (Seed) no momento da criação do banco
        inserirPerguntaInicial(
            db,
            "Qual é o maior planeta do Sistema Solar?",
            "Saturno", "Júpiter", "Netuno",
            1
        )
        inserirPerguntaInicial(
            db,
            "Em que ano o homem pisou na Lua pela primeira vez?",
            "1969", "1975", "1961",
            0
        )
        inserirPerguntaInicial(
            db,
            "Qual elemento químico tem o símbolo 'O' na tabela periódica?",
            "Ouro", "Ósmio", "Oxigênio",
            2
        )
        inserirPerguntaInicial(
            db,
            "Qual país tem o formato parecido com uma bota no mapa?",
            "Espanha", "Itália", "Grécia",
            1
        )
        inserirPerguntaInicial(
            db,
            "Quantos continentes existem na Terra segundo o modelo tradicional?",
            "6 continentes", "4 continentes", "8 continentes",
            0
        )
    }

    // Usa o parâmetro 'db' diretamente para evitar chamadas recursivas no onCreate
    private fun inserirPerguntaInicial(
        db: SQLiteDatabase,
        enunciado: String,
        a: String,
        b: String,
        c: String,
        correta: Int
    ) {
        val valores = ContentValues().apply {
            put("enunciado", enunciado)
            put("opcao_a", a)
            put("opcao_b", b)
            put("opcao_c", c)
            put("correta", correta)
        }
        db.insert("perguntas", null, valores)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS perguntas")
        onCreate(db)
    }

    // READ: Lê todas as perguntas armazenadas no SQLite
    fun buscarPerguntas(): List<Pergunta> {
        val lista = mutableListOf<Pergunta>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM perguntas", null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val enunciado = it.getString(it.getColumnIndexOrThrow("enunciado"))
                val opA = it.getString(it.getColumnIndexOrThrow("opcao_a"))
                val opB = it.getString(it.getColumnIndexOrThrow("opcao_b"))
                val opC = it.getString(it.getColumnIndexOrThrow("opcao_c"))
                val correta = it.getInt(it.getColumnIndexOrThrow("correta"))

                lista.add(Pergunta(id, enunciado, listOf(opA, opB, opC), correta))
            }
        }
        db.close()
        return lista
    }
    fun salvarRecorde(pontos: Int) {
        val db = writableDatabase
        val valores = ContentValues().apply { put("pontos", pontos) }
        db.insert("recordes", null, valores)
        db.close()
    }

    fun buscarMelhorPontuacao(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT MAX(pontos) FROM recordes", null)
        var recorde = 0
        cursor.use {
            if (it.moveToFirst()) recorde = it.getInt(0)
        }
        db.close()
        return recorde
    }

    fun inserirPergunta(enunciado: String, a: String, b: String, c: String, correta: Int) {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("enunciado", enunciado)
            put("opcao_a", a)
            put("opcao_b", b)
            put("opcao_c", c)
            put("correta", correta)
        }
        db.insert("perguntas", null, valores)
        db.close()
    }
}

