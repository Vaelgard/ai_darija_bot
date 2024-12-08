document.addEventListener('DOMContentLoaded', () => {

  document.getElementById('chat-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const messageInput = document.getElementById('message');
    const userMessage = messageInput.value.trim();


    if (userMessage) {

      appendMessage('You', userMessage, 'user');
      messageInput.value = '';

      try {

        const response = await fetch('sendMessage.php', {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: `message=${encodeURIComponent(userMessage)}`,
        });

        const jsonResponse = await response.json();


        if (jsonResponse.error) {
          appendMessage('Bot', `Error: ${jsonResponse.error}`, 'bot');
        } else {

          const translatedText = jsonResponse.translated || 'Translation unavailable.';
          appendMessage('Bot', `${translatedText}`, 'bot');
        }
      } catch (error) {

        appendMessage('Bot', 'Error: Could not connect to server.', 'bot');
      }
    }
  });


  function appendMessage(sender, text, cssClass) {
    const chatWindow = document.getElementById('chat-window');


    if (!chatWindow) {
      console.error('Chat window element not found.');
      return;
    }


    const messageDiv = document.createElement('div');
    messageDiv.classList.add('chat-message', cssClass);


    messageDiv.innerHTML = `<strong>${sender}:</strong> ${text}`;


    chatWindow.appendChild(messageDiv);


    chatWindow.scrollTop = chatWindow.scrollHeight;
  }
});
