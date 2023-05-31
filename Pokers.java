package Pokers;

import java.util.*;

enum Suit {
    SPADES("スペード"), HEARTS("ハート"), DIAMONDS("ダイヤ"), CLUBS("クラブ");

    private String displayName;

    Suit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

enum Rank {
    TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"), EIGHT("8"),
    NINE("9"), TEN("10"), JACK("ジャック"), QUEEN("クイーン"), KING("キング"), ACE("エース");

    private String displayName;

    Rank(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

class Card {
    private Suit suit;
    private Rank rank;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public String toString() {
        return rank.getDisplayName() + "の" + suit.getDisplayName();
    }
}

enum HandRank {
    HIGH_CARD("ハイカード", 1), ONE_PAIR("ワンペア", 2), TWO_PAIR("ツーペア", 3), THREE_OF_A_KIND("スリーカード", 4),
    STRAIGHT("ストレート", 5), FLUSH("フラッシュ", 6), FULL_HOUSE("フルハウス", 7), FOUR_OF_A_KIND("フォーカード", 8),
    STRAIGHT_FLUSH("ストレートフラッシュ", 9), ROYAL_FLUSH("ロイヤルフラッシュ", 10);

    private String displayName;
    private int score;

    HandRank(String displayName, int score) {
        this.displayName = displayName;
        this.score = score;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getScore() {
        return score;
    }
}

class Player {
    private String name;
    private List<Card> hand;
    private HandRank handRank;
    private int score;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return hand;
    }

    public HandRank getHandRank() {
        return handRank;
    }

    public int getScore() {
        return score;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }
    
    public void replaceCards(List<Integer> indices, List<Card> newCards) {
        if (indices.size() != newCards.size()) {
            System.out.println("入れ替えるカードの数と新しいカードの数が一致しません。入れ替えは行われません。");
            return;
        }

        for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);
            if (index >= 0 && index < hand.size()) {
                hand.set(index, newCards.get(i));
            } else {
                System.out.println("入れ替えるカードの番号が手札の範囲外です。入れ替えは行われません。");
                return;
            }
        }
    }

    public void calculateScore() {
        handRank = evaluateHandRank();
        score = handRank.getScore();
    }

    private HandRank evaluateHandRank() {
        Map<Rank, Integer> rankCounts = new HashMap<>();
        Map<Suit, Integer> suitCounts = new HashMap<>();
        boolean hasStraight = false;
        boolean hasFlush = false;

        // 手札のカードをランクとスートでグループ化し、それぞれの出現回数を数える
        for (Card card : hand) {
            Rank rank = card.getRank();
            Suit suit = card.getSuit();

            rankCounts.put(rank, rankCounts.getOrDefault(rank, 0) + 1);
            suitCounts.put(suit, suitCounts.getOrDefault(suit, 0) + 1);
        }

        // ロイヤルフラッシュの判定
        if (suitCounts.containsValue(5) && rankCounts.containsKey(Rank.TEN) && rankCounts.containsKey(Rank.JACK)
                && rankCounts.containsKey(Rank.QUEEN) && rankCounts.containsKey(Rank.KING)
                && rankCounts.containsKey(Rank.ACE)) {
            return HandRank.ROYAL_FLUSH;
        }

        // ストレートフラッシュの判定
        if (suitCounts.containsValue(5) && hasStraight(rankCounts)) {
            return HandRank.STRAIGHT_FLUSH;
        }

        // フォーカードの判定
        if (rankCounts.containsValue(4)) {
            return HandRank.FOUR_OF_A_KIND;
        }

        // フルハウスの判定
        if (rankCounts.containsValue(3) && rankCounts.containsValue(2)) {
            return HandRank.FULL_HOUSE;
        }

        // フラッシュの判定
        if (suitCounts.containsValue(5)) {
            return HandRank.FLUSH;
        }

        // ストレートの判定
        if (hasStraight(rankCounts)) {
            return HandRank.STRAIGHT;
        }

        // スリーカードの判定
        if (rankCounts.containsValue(3)) {
            return HandRank.THREE_OF_A_KIND;
        }

        // ツーペアの判定
        if (getPairCount(rankCounts) == 2) {
            return HandRank.TWO_PAIR;
        }

        // ワンペアの判定
        if (getPairCount(rankCounts) == 1) {
            return HandRank.ONE_PAIR;
        }

        // ハイカード
        return HandRank.HIGH_CARD;
    }

    // ストレートの判定メソッド
    private boolean hasStraight(Map<Rank, Integer> rankCounts) {
        int straightCount = 0;
        for (Rank rank : Rank.values()) {
            if (rankCounts.containsKey(rank)) {
                straightCount++;
            } else {
                straightCount = 0;
            }
            if (straightCount == 5) {
                return true;
            }
        }
        return false;
    }

    // ペアの数を数えるメソッド
    private int getPairCount(Map<Rank, Integer> rankCounts) {
        int pairCount = 0;
        for (int count : rankCounts.values()) {
            if (count == 2) {
                pairCount++;
            }
        }
        return pairCount;
    }

}

class PokerGame {
    private List<Player> players;
    private List<Card> deck;

    public PokerGame() {
        players = new ArrayList<>();
        deck = generateDeck();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void startGame() {
        dealCards();
        evaluateHands();
        replaceCards();
        evaluateHands();
        determineWinner();
    }

    private void replaceCards() {
        Scanner scanner = new Scanner(System.in);

        for (Player player : players) {
            System.out.println(player.getName() + "さんの手札: " + player.getHand());
            System.out.print("入れ替えるカードの番号をスペース区切りで入力してください (1-5)。入れ替えない場合は何も入力せずにEnterを押してください: ");
            String input = scanner.nextLine();

            if (input.isEmpty()) {
                continue; // 入れ替えない場合は次のプレイヤーへ
            }

            String[] indicesStr = input.split("\\s+");
            List<Integer> indices = new ArrayList<>();
            for (String indexStr : indicesStr) {
                indices.add(Integer.parseInt(indexStr) - 1); // ユーザーの入力を0-indexedに変換
            }

            if (indices.size() > 0 && indices.size() <= 5) {
                List<Card> newCards = new ArrayList<>();
                for (int i = 0; i < indices.size(); i++) {
                    Card newCard = deck.remove(0); // デッキからランダムにカードを選ぶ
                    newCards.add(newCard);
                }

                player.replaceCards(indices, newCards);
            } else {
                System.out.println("入力されたカードの番号が無効です。入れ替えは行われません。");
            }
        }
    }



    private List<Card> generateDeck() {
        List<Card> deck = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                Card card = new Card(suit, rank);
                deck.add(card);
            }
        }
        return deck;
    }

    private void shuffleDeck() {
        Collections.shuffle(deck);
    }

    private void dealCards() {
        shuffleDeck();
        int cardsToDeal = 5; // 手札の枚数
        for (Player player : players) {
            List<Card> hand = new ArrayList<>();
            for (int i = 0; i < cardsToDeal && !deck.isEmpty(); i++) { // デッキにカードが残っている場合にのみカードを配る
                Card card = deck.remove(0);
                hand.add(card);
            }
            player.setHand(hand);
        }
    }

    private void evaluateHands() {
        for (Player player : players) {
            player.calculateScore();
            HandRank handRank = player.getHandRank();
            System.out.println(player.getName() + "の役: " + handRank.getDisplayName());
            System.out.println("手札: " + player.getHand());
            System.out.println("得点: " + player.getScore());
            System.out.println("----------------------------------");
        }
    }

    private void determineWinner() {
        Player winner = players.get(0); // 初期値として最初のプレイヤーを仮の勝者とする

        for (int i = 1; i < players.size(); i++) {
            Player currentPlayer = players.get(i);
            if (currentPlayer.getScore() > winner.getScore()) {
                winner = currentPlayer; // より高い得点のプレイヤーを新たな勝者とする
            } else if (currentPlayer.getScore() == winner.getScore()) {
                // 得点が同じ場合は、役の強さで判定
                if (currentPlayer.getHandRank().getScore() > winner.getHandRank().getScore()) {
                    winner = currentPlayer;
                }
            }
        }

        System.out.println("勝者: " + winner.getName());
        System.out.println("役: " + winner.getHandRank().getDisplayName());
    }
}

//既存のクラスとメソッドは省略

public class Pokers {
 public static void main(String[] args) {
     Scanner scanner = new Scanner(System.in);
     boolean continueGame = true;

     while (continueGame) {
         PokerGame game = new PokerGame();

         System.out.println("=== ポーカーゲーム ===");
         System.out.println("ゲームのルール: 手札を交換して最も強い役を作り、他のプレイヤーよりも勝ちましょう！");
         System.out.println("手札は5枚配られます。交換したいカードの枚数を入力してください。");
         System.out.println("役の強さは以下の通りです:");
         for (HandRank handRank : HandRank.values()) {
             System.out.println(handRank.getDisplayName() + " - " + handRank.getScore() + "点");
         }
         System.out.println();

         System.out.print("プレイヤーの人数を入力してください: ");
         int playerCount = scanner.nextInt();

         scanner.nextLine(); // 改行読み捨て

         for (int i = 1; i <= playerCount; i++) {
             System.out.print("プレイヤー" + i + "の名前を入力してください: ");
             String playerName = scanner.nextLine();
             Player player = new Player(playerName);
             game.addPlayer(player);
         }

         game.startGame();

         System.out.print("ゲームを続けますか？ (yes/no): ");
         String playAgain = scanner.nextLine();
         continueGame = playAgain.equalsIgnoreCase("yes") || playAgain.equalsIgnoreCase("y");

         if (!continueGame) {
             System.out.println("ゲームを終了します。お疲れ様でした！");
         }
     }
 }
}
