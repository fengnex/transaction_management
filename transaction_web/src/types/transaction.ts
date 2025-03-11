export interface Transaction {
  id: number;
  amount: number;
  type: string;
  category: string;
  status: string;
  riskLevel: string;
  exchangeRate: number;
  sourceAccountNumber: string;
  destinationAccountNumber: string;
  remarks: string;
  timestamp: string;
}

export interface TransactionFormData {
  amount: number;
  type: string;
  category: string;
  status: string;
  riskLevel: string;
  exchangeRate: number;
  sourceAccountNumber: string;
  destinationAccountNumber: string;
  remarks: string;
  timestamp: string;
} 