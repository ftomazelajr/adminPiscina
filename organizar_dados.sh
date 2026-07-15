#!/bin/bash

echo "========================================="
echo "  ORGANIZADOR DE DADOS - TOMAZELA"
echo "========================================="
echo ""

# Seu UID
UID="bjtxYFDOBaOXuc60V8YEluuaTpp1"

echo "📋 Verificando dados..."

# Verificar se os dados já estão na nova estrutura
echo "Verificando dados em dados/$UID..."
curl -s "https://tomazela-piscinas-default-rtdb.firebaseio.com/dados/$UID/clientes.json" | head -c 100

echo ""
echo "========================================="
echo "  INSTRUÇÕES PARA O FIREBASE"
echo "========================================="
echo ""
echo "1. Abra o navegador e acesse:"
echo "   https://console.firebase.google.com/"
echo ""
echo "2. Selecione o projeto: tomazela-piscinas"
echo ""
echo "3. Vá em Realtime Database → Regras"
echo ""
echo "4. Cole estas regras e clique em PUBLICAR:"
echo ""
echo '{
  "rules": {
    ".read": true,
    ".write": true
  }
}'
echo ""
echo "========================================="
echo "   DEPOIS DE PUBLICAR, TESTE O APP"
echo "========================================="
echo ""
echo "Se os clientes aparecerem, está tudo certo!"
echo ""
echo "Depois volte e cole as regras seguras:"
echo ""
echo '{
  "rules": {
    "dados": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}'
echo ""
echo "========================================="
