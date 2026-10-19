import raw from './content.json';

export interface LearningItem {
  id: string;
  title: string;
  subtitle: string;
  category: 'ALPHABET' | 'VOWEL' | 'NUMBER' | 'VOCABULARY';
  level: number;
  signName: string;
  visualCueSteps: string[];
  visualItemsCount: number;
  objectEmoji: string;
  funFact: string;
  handPoseType: string;
  colorHex: number;
  associatedWord: string;
}

export interface GameLevel {
  id: number;
  title: string;
  subtitle: string;
  description: string;
  requiredStars: number;
  badgeReward: string;
  badgeEmoji: string;
  itemIds: string[];
  colorHex: number;
}

export interface StickerBadge {
  id: string;
  title: string;
  description: string;
  iconEmoji: string;
  requiredStars: number;
  category: string;
}

export interface GesturePreset {
  id: string;
  name: string;
  emoji: string;
  bodyDescription: string;
  alphabetSymbol: string;
  numberSymbol: string;
  alphabetWord: string;
  numberWord: string;
  pose: string;
  funHint: string;
}

export interface Content {
  generated: string;
  alphabetItems: LearningItem[];
  englishAlphabetItems: LearningItem[];
  numberItems: LearningItem[];
  vocabularyItems: LearningItem[];
  levels: GameLevel[];
  badges: StickerBadge[];
  presets: GesturePreset[];
  wordLabels: Record<string, string>;
}

export const content = raw as unknown as Content;

export const allItems: LearningItem[] = [
  ...content.alphabetItems,
  ...content.englishAlphabetItems,
  ...content.numberItems,
  ...content.vocabularyItems,
];

export const itemById = new Map(allItems.map((i) => [i.id, i]));

export function wordLabel(id: string): string {
  const item = itemById.get(id);
  if (!item) return id;
  if (content.wordLabels[id]) return content.wordLabels[id];
  if (item.associatedWord) return item.associatedWord;
  if (item.category === 'NUMBER') return `Number ${item.title}`;
  return item.title;
}
