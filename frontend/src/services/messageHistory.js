export function normalizeRoleType(rawType) {
  if (rawType == null) {
    return 'assistant';
  }

  const role = typeof rawType === 'object' && rawType.name
    ? rawType.name
    : rawType;

  const normalizedRole = String(role).toUpperCase();
  return normalizedRole === 'USER' || normalizedRole === 'HUMAN'
    ? 'user'
    : 'assistant';
}

export function parseMessageTime(message) {
  const timestamp = message?.timestamp;
  if (timestamp == null || timestamp === '') {
    return null;
  }

  if (typeof timestamp === 'number') {
    return Number.isFinite(timestamp) ? timestamp : null;
  }

  if (typeof timestamp === 'string') {
    const parsed = Date.parse(timestamp);
    return Number.isNaN(parsed) ? null : parsed;
  }

  if (Array.isArray(timestamp) && timestamp.length >= 3) {
    const parsed = new Date(
      timestamp[0],
      timestamp[1] - 1,
      timestamp[2],
      timestamp[3] || 0,
      timestamp[4] || 0,
      timestamp[5] || 0
    ).getTime();
    return Number.isNaN(parsed) ? null : parsed;
  }

  return null;
}

export function normalizePersistedMessages(messages) {
  if (!Array.isArray(messages)) {
    return [];
  }

  return messages
    .map((message, index) => {
      const text = message?.message ?? message?.content ?? '';

      return {
        type: normalizeRoleType(message?.type),
        content: String(text),
        isNew: false,
        copied: message?.copied === true,
        agent: message?.agent ?? null,
        sequence: index,
        _timestamp: parseMessageTime(message),
        _index: index
      };
    })
    .sort((left, right) => {
      const leftTime = left._timestamp ?? Number.POSITIVE_INFINITY;
      const rightTime = right._timestamp ?? Number.POSITIVE_INFINITY;
      return leftTime - rightTime || left._index - right._index;
    })
    .map(({ _timestamp, _index, ...message }) => message);
}
