#!/bin/bash

# Test login API with user1
echo "Testing login API with user1..."
response=$(curl -s -X POST http://localhost:1444/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"email": "testuser@example.com", "password": "123123a"}')

echo "Login response: $response"

# Extract token if login successful
token=$(echo $response | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ ! -z "$token" ]; then
  echo "Token extracted: $token"
  
  # Test user profile API
  echo "Testing user profile API..."
  profile_response=$(curl -s -X GET http://localhost:1444/api/user/profile \
    -H "Authorization: Bearer $token")
  
  echo "Profile response: $profile_response"
  
  # Test open matches API
  echo "\n=== Testing /api/open-matches endpoint ==="
  matches_response=$(curl -s -X GET http://localhost:1444/api/open-matches \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json")
  
  echo "Open matches response:"
  echo "$matches_response" | jq .
  
  # Also test draft matches endpoint
  echo "\n=== Testing /api/draft-matches endpoint ==="
  draft_matches_response=$(curl -s -X GET "http://localhost:1444/api/draft-matches" \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json")

  echo "Draft matches response:"
  echo "$draft_matches_response" | jq .

  # Check if AI service is being called by looking for compatibility scores
  echo "\n=== Checking for compatibility scores ==="
  if echo "$matches_response" | jq -e '.[] | select(.compatibilityScore != null)' > /dev/null 2>&1; then
      echo "✓ Found compatibility scores in open matches"
  else
      echo "✗ No compatibility scores found in open matches"
  fi

  if echo "$draft_matches_response" | jq -e '.[] | select(.compatibilityScore != null)' > /dev/null 2>&1; then
      echo "✓ Found compatibility scores in draft matches"
  else
      echo "✗ No compatibility scores found in draft matches"
  fi
else
  echo "Login failed or no token received"
fi