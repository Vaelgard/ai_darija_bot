document.addEventListener('DOMContentLoaded', () => {
  // Event listener for the form submit
  document.getElementById('chat-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const messageInput = document.getElementById('message');
    const userMessage = messageInput.value.trim();

    // Check if the user has typed a message
    if (userMessage) {
      // Append the user's message to the chat
      appendMessage('You', userMessage, 'user');
      messageInput.value = ''; // Clear the input field

      try {
        // Send the user's message to the PHP backend
        const response = await fetch('sendMessage.php', {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: `message=${encodeURIComponent(userMessage)}`,
        });

        const jsonResponse = await response.json();

        // Check if there's an error in the response
        if (jsonResponse.error) {
          appendMessage('Bot', `Error: ${jsonResponse.error}`, 'bot');
        } else {
          // Show the translated text (or a fallback if not available)
          const translatedText = jsonResponse.translated || 'Translation unavailable.';
          appendMessage('Bot', `${translatedText}`, 'bot');
        }
      } catch (error) {
        // Handle any connection errors
        appendMessage('Bot', 'Error: Could not connect to server.', 'bot');
      }
    }
  });

  // Function to append messages to the chat
  function appendMessage(sender, text, cssClass) {
    const chatWindow = document.getElementById('chat-window');

    // Check if chatWindow exists
    if (!chatWindow) {
      console.error('Chat window element not found.');
      return;
    }

    // Create a new div for the message
    const messageDiv = document.createElement('div');
    messageDiv.classList.add('chat-message', cssClass);

    // Format the message content
    messageDiv.innerHTML = `<strong>${sender}:</strong> ${text}`;

    // Append the new message to the chat window
    chatWindow.appendChild(messageDiv);

    // Scroll to the bottom of the chat window
    chatWindow.scrollTop = chatWindow.scrollHeight;
  }
});
