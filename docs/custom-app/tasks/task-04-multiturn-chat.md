# Task 04: Multi-turn Chat Core

## Goal

Reuse the useful part of the current chat logic in a compact chat/evaluate screen.

## Scope

- create a compact chat screen
- user can send a prompt
- model generates a response
- assistant response is stored
- next user turn runs with prior user and assistant messages in context
- add clear/reset conversation action

## Reuse Targets

- `SmolLMManager`
- current message/chat persistence concept
- current model load and generation flow

## Completion Criteria

- a second turn clearly uses the first-turn history
- prior assistant output is automatically included in next-turn inference context

