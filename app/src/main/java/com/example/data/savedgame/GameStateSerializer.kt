package com.example.data.savedgame

import com.example.game.CardModel
import com.example.game.CardRank
import com.example.game.Suit
import com.example.game.klondike.DifficultyMode
import com.example.game.klondike.DrawMode
import com.example.game.klondike.KlondikeGameState
import org.json.JSONArray
import org.json.JSONObject

object GameStateSerializer {

  private fun cardToJson(card: CardModel): JSONObject {
    return JSONObject().apply {
      put("r", card.rank.name)
      put("s", card.suit.name)
      put("u", card.isFaceUp)
    }
  }

  private fun jsonToCard(obj: JSONObject): CardModel {
    return CardModel(
      rank = CardRank.valueOf(obj.getString("r")),
      suit = Suit.valueOf(obj.getString("s")),
      isFaceUp = obj.getBoolean("u"),
    )
  }

  private fun cardListToJson(list: List<CardModel>): JSONArray {
    val array = JSONArray()
    for (card in list) {
      array.put(cardToJson(card))
    }
    return array
  }

  private fun jsonToCardList(array: JSONArray): List<CardModel> {
    val list = ArrayList<CardModel>(array.length())
    for (i in 0 until array.length()) {
      list.add(jsonToCard(array.getJSONObject(i)))
    }
    return list
  }

  fun serializeState(state: KlondikeGameState): String {
    val json = JSONObject()
    json.put("difficulty", state.difficulty.name)
    json.put("drawMode", state.drawMode.name)
    json.put("moveCount", state.moveCount)
    json.put("elapsedSeconds", state.elapsedSeconds)
    json.put("baseScore", state.baseScore)
    json.put("undosUsed", state.undosUsed)
    json.put("cardsPlayedSinceStockRecycle", state.cardsPlayedSinceStockRecycle)
    json.put("isWon", state.isWon)
    json.put("hasNoMovesRemaining", state.hasNoMovesRemaining)

    // Stock
    json.put("stock", cardListToJson(state.stock))
    // Waste
    json.put("waste", cardListToJson(state.waste))

    // Foundations
    val foundationsJson = JSONObject()
    for ((suit, cards) in state.foundations) {
      foundationsJson.put(suit.name, cardListToJson(cards))
    }
    json.put("foundations", foundationsJson)

    // Tableau
    val tableauArray = JSONArray()
    for (col in state.tableau) {
      tableauArray.put(cardListToJson(col))
    }
    json.put("tableau", tableauArray)

    return json.toString()
  }

  fun deserializeState(jsonStr: String): KlondikeGameState {
    val json = JSONObject(jsonStr)

    val difficulty = DifficultyMode.valueOf(json.optString("difficulty", DifficultyMode.REGULAR.name))
    val drawMode = DrawMode.valueOf(json.optString("drawMode", DrawMode.DRAW_ONE.name))
    val moveCount = json.optInt("moveCount", 0)
    val elapsedSeconds = json.optLong("elapsedSeconds", 0L)
    val baseScore = json.optInt("baseScore", 0)
    val undosUsed = json.optInt("undosUsed", 0)
    val cardsPlayed = json.optInt("cardsPlayedSinceStockRecycle", 0)
    val isWon = json.optBoolean("isWon", false)
    val hasNoMoves = json.optBoolean("hasNoMovesRemaining", false)

    val stock = jsonToCardList(json.getJSONArray("stock"))
    val waste = jsonToCardList(json.getJSONArray("waste"))

    val foundations = mutableMapOf<Suit, List<CardModel>>()
    val foundationsJson = json.getJSONObject("foundations")
    for (suit in Suit.entries) {
      val pileArray = foundationsJson.optJSONArray(suit.name)
      foundations[suit] = if (pileArray != null) jsonToCardList(pileArray) else emptyList()
    }

    val tableauArray = json.getJSONArray("tableau")
    val tableau = (0 until 7).map { colIdx ->
      if (colIdx < tableauArray.length()) {
        jsonToCardList(tableauArray.getJSONArray(colIdx))
      } else {
        emptyList()
      }
    }

    return KlondikeGameState(
      stock = stock,
      waste = waste,
      foundations = foundations,
      tableau = tableau,
      drawMode = drawMode,
      difficulty = difficulty,
      moveCount = moveCount,
      elapsedSeconds = elapsedSeconds,
      baseScore = baseScore,
      undosUsed = undosUsed,
      cardsPlayedSinceStockRecycle = cardsPlayed,
      isWon = isWon,
      hasNoMovesRemaining = hasNoMoves,
    )
  }

  fun serializeUndoStack(stack: List<KlondikeGameState>): String {
    val array = JSONArray()
    for (state in stack) {
      array.put(serializeState(state))
    }
    return array.toString()
  }

  fun deserializeUndoStack(jsonStr: String): List<KlondikeGameState> {
    if (jsonStr.isBlank()) return emptyList()
    val array = JSONArray(jsonStr)
    val list = ArrayList<KlondikeGameState>(array.length())
    for (i in 0 until array.length()) {
      list.add(deserializeState(array.getString(i)))
    }
    return list
  }
}
