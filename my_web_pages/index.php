<?php
session_start();

// Initialize game state
if (!isset($_SESSION['board']) || isset($_GET['reset'])) {
    $_SESSION['board']   = array_fill(0, 9, '');
    $_SESSION['status']  = 'playing'; // 'playing', 'win_X', 'win_O', 'draw'
    $_SESSION['message'] = 'A tua vez! Joga com ❌';
}

$board   = &$_SESSION['board'];
$status  = &$_SESSION['status'];
$message = &$_SESSION['message'];

// Win combinations
$wins = [
    [0,1,2],[3,4,5],[6,7,8], // rows
    [0,3,6],[1,4,7],[2,5,8], // cols
    [0,4,8],[2,4,6]           // diags
];

function checkWinner($b, $wins) {
    foreach ($wins as $combo) {
        if ($b[$combo[0]] !== '' && $b[$combo[0]] === $b[$combo[1]] && $b[$combo[1]] === $b[$combo[2]]) {
            return $b[$combo[0]];
        }
    }
    return null;
}

function isBoardFull($b) {
    return !in_array('', $b);
}

// AI: returns best move index for 'O', or -1 if none
function aiMove($b, $wins) {
    // 1. Win if possible
    foreach ($wins as $combo) {
        $cells = [$b[$combo[0]], $b[$combo[1]], $b[$combo[2]]];
        $oCount = array_count_values($cells)['O'] ?? 0;
        $empty  = array_search('', $cells);
        if ($oCount === 2 && $empty !== false) return $combo[$empty];
    }
    // 2. Block player from winning
    foreach ($wins as $combo) {
        $cells = [$b[$combo[0]], $b[$combo[1]], $b[$combo[2]]];
        $xCount = array_count_values($cells)['X'] ?? 0;
        $empty  = array_search('', $cells);
        if ($xCount === 2 && $empty !== false) return $combo[$empty];
    }
    // 3. Take center
    if ($b[4] === '') return 4;
    // 4. Take a corner
    foreach ([0,2,6,8] as $c) { if ($b[$c] === '') return $c; }
    // 5. Take any side
    foreach ([1,3,5,7] as $c) { if ($b[$c] === '') return $c; }
    return -1;
}

// Process player move
if ($status === 'playing' && isset($_POST['cell'])) {
    $cell = (int)$_POST['cell'];
    if ($cell >= 0 && $cell < 9 && $board[$cell] === '') {
        // Player move
        $board[$cell] = 'X';
        $winner = checkWinner($board, $wins);
        if ($winner) {
            $status  = 'win_X';
            $message = '🎉 Ganhaste! Parabéns!';
        } elseif (isBoardFull($board)) {
            $status  = 'draw';
            $message = '🤝 Empate! Bem jogado!';
        } else {
            // AI move
            $aiIdx = aiMove($board, $wins);
            if ($aiIdx >= 0) {
                $board[$aiIdx] = 'O';
                $winner = checkWinner($board, $wins);
                if ($winner) {
                    $status  = 'win_O';
                    $message = '🤖 O computador ganhou! Tenta de novo!';
                } elseif (isBoardFull($board)) {
                    $status  = 'draw';
                    $message = '🤝 Empate! Bem jogado!';
                } else {
                    $message = 'A tua vez! Joga com ❌';
                }
            }
        }
    }
}

// Determine winning cells for highlight
$winCells = [];
if (in_array($status, ['win_X','win_O'])) {
    foreach ($wins as $combo) {
        if ($board[$combo[0]] !== '' && $board[$combo[0]] === $board[$combo[1]] && $board[$combo[1]] === $board[$combo[2]]) {
            $winCells = $combo;
            break;
        }
    }
}
?>
<!DOCTYPE html>
<html lang="pt">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tic-Tac-Toe — Humano vs Computador</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&display=swap');

        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

        body {
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #0f0f1a;
            background-image:
                radial-gradient(ellipse at 20% 20%, rgba(99,102,241,0.15) 0%, transparent 60%),
                radial-gradient(ellipse at 80% 80%, rgba(236,72,153,0.12) 0%, transparent 60%);
            font-family: 'Outfit', sans-serif;
            color: #e2e8f0;
        }

        .container {
            text-align: center;
            padding: 2rem 1.5rem;
            width: 100%;
            max-width: 480px;
        }

        .logo {
            font-size: 2.8rem;
            font-weight: 900;
            letter-spacing: -1px;
            background: linear-gradient(135deg, #818cf8, #ec4899);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            margin-bottom: 0.25rem;
        }

        .subtitle {
            font-size: 0.9rem;
            color: #64748b;
            font-weight: 300;
            letter-spacing: 2px;
            text-transform: uppercase;
            margin-bottom: 2rem;
        }

        /* Score board */
        .scoreboard {
            display: flex;
            justify-content: center;
            gap: 1rem;
            margin-bottom: 1.5rem;
        }
        .score-card {
            background: rgba(255,255,255,0.05);
            border: 1px solid rgba(255,255,255,0.1);
            border-radius: 14px;
            padding: 0.75rem 1.5rem;
            min-width: 100px;
        }
        .score-card .label { font-size: 0.7rem; color: #64748b; letter-spacing: 1px; text-transform: uppercase; }
        .score-card .value { font-size: 1.8rem; font-weight: 700; }
        .score-card.player .value { color: #818cf8; }
        .score-card.cpu    .value { color: #ec4899; }
        .score-card.draw   .value { color: #94a3b8; }

        /* Message */
        .message-box {
            background: rgba(255,255,255,0.06);
            border: 1px solid rgba(255,255,255,0.1);
            border-radius: 12px;
            padding: 0.85rem 1.5rem;
            font-size: 1.05rem;
            font-weight: 600;
            margin-bottom: 1.5rem;
            min-height: 52px;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s ease;
        }

        /* Board */
        .board {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 10px;
            margin: 0 auto 1.5rem;
            max-width: 340px;
        }

        .cell {
            aspect-ratio: 1;
            background: rgba(255,255,255,0.05);
            border: 2px solid rgba(255,255,255,0.1);
            border-radius: 16px;
            font-size: 2.5rem;
            cursor: pointer;
            transition: all 0.2s ease;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 900;
            position: relative;
            overflow: hidden;
        }

        .cell:hover:not(.taken):not(.disabled) {
            background: rgba(129,140,248,0.15);
            border-color: rgba(129,140,248,0.5);
            transform: scale(1.03);
        }

        .cell.taken { cursor: default; }
        .cell.disabled { cursor: not-allowed; opacity: 0.7; }

        .cell.X { color: #818cf8; border-color: rgba(129,140,248,0.4); }
        .cell.O { color: #ec4899; border-color: rgba(236,72,153,0.4); }

        .cell.winning {
            background: rgba(255,255,255,0.12);
            border-color: #fbbf24;
            box-shadow: 0 0 20px rgba(251,191,36,0.4);
            animation: pulse-win 1s ease-in-out infinite alternate;
        }

        @keyframes pulse-win {
            from { box-shadow: 0 0 10px rgba(251,191,36,0.3); }
            to   { box-shadow: 0 0 30px rgba(251,191,36,0.7); }
        }

        /* Buttons */
        .btn-reset {
            display: inline-block;
            padding: 0.85rem 2.5rem;
            background: linear-gradient(135deg, #6366f1, #ec4899);
            color: #fff;
            border: none;
            border-radius: 50px;
            font-family: 'Outfit', sans-serif;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            text-decoration: none;
            transition: all 0.25s ease;
            letter-spacing: 0.5px;
        }
        .btn-reset:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(99,102,241,0.5);
            opacity: 0.95;
        }

        .footer-note {
            margin-top: 1.5rem;
            font-size: 0.75rem;
            color: #334155;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="logo">Tic‑Tac‑Toe</div>
    <div class="subtitle">Humano vs Computador</div>

    <!-- Score (session-based) -->
    <?php
    if (!isset($_SESSION['score'])) $_SESSION['score'] = ['X'=>0,'O'=>0,'D'=>0];
    if ($status === 'win_X')   $_SESSION['score']['X']++;
    elseif ($status === 'win_O') $_SESSION['score']['O']++;
    elseif ($status === 'draw')  $_SESSION['score']['D']++;
    // Prevent double-counting on refresh: lock score after update
    if (in_array($status, ['win_X','win_O','draw'])) $status = $status . '_counted';
    ?>
    <div class="scoreboard">
        <div class="score-card player">
            <div class="label">Tu ❌</div>
            <div class="value"><?= $_SESSION['score']['X'] ?></div>
        </div>
        <div class="score-card draw">
            <div class="label">Empate</div>
            <div class="value"><?= $_SESSION['score']['D'] ?></div>
        </div>
        <div class="score-card cpu">
            <div class="label">CPU 🔴</div>
            <div class="value"><?= $_SESSION['score']['O'] ?></div>
        </div>
    </div>

    <!-- Message -->
    <div class="message-box"><?= htmlspecialchars($message) ?></div>

    <!-- Board -->
    <form method="POST" id="gameForm">
        <div class="board">
            <?php for ($i = 0; $i < 9; $i++):
                $val     = $board[$i];
                $label   = $val === 'X' ? '❌' : ($val === 'O' ? '🔴' : '');
                $taken   = $val !== '';
                $gameOver = strpos($status, 'win') !== false || $status === 'draw' || strpos($status, '_counted') !== false;
                $classes = 'cell';
                if ($taken)    $classes .= " taken $val";
                if ($gameOver && !$taken) $classes .= ' disabled';
                if (in_array($i, $winCells)) $classes .= ' winning';
            ?>
                <button
                    type="submit"
                    name="cell"
                    value="<?= $i ?>"
                    class="<?= $classes ?>"
                    <?= ($taken || $gameOver) ? 'disabled' : '' ?>
                >
                    <?= $label ?>
                </button>
            <?php endfor; ?>
        </div>
    </form>

    <!-- Reset -->
    <a href="?reset=1" class="btn-reset">🔄 Novo Jogo</a>

    <div class="footer-note">Ficha Laboratorial — Engenharia de Software · ISCTE-IUL</div>
</div>
</body>
</html>
