import React from 'react';
import { Table, Button, Space, Popconfirm } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { Transaction } from '../types/transaction';
import { EditOutlined, DeleteOutlined } from '@ant-design/icons';

interface TransactionListProps {
  transactions: Transaction[];
  onEdit: (transaction: Transaction) => void;
  onDelete: (id: number) => void;
}

const TransactionList: React.FC<TransactionListProps> = ({
  transactions,
  onEdit,
  onDelete,
}) => {
  const columns: ColumnsType<Transaction> = [
    {
      title: 'Timestamp',
      dataIndex: 'timestamp',
      key: 'timestamp',
      sorter: (a: Transaction, b: Transaction) =>
        new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime(),
    },
    {
      title: 'Type',
      dataIndex: 'type',
      key: 'type',
      filters: [
        { text: 'Deposit', value: 'DEPOSIT' },
        { text: 'Withdrawal', value: 'WITHDRAWAL' },
        { text: 'Transfer', value: 'TRANSFER' },
      ],
      onFilter: (value: any, record: Transaction) => record.type === value,
    },
    {
      title: 'Category',
      dataIndex: 'category',
      key: 'category',
    },
    {
      title: 'Amount',
      dataIndex: 'amount',
      key: 'amount',
      sorter: (a: Transaction, b: Transaction) => a.amount - b.amount,
      render: (amount: number, record: Transaction) => {
        const color = record.type === 'INCOME' ? '#52c41a' : '#f5222d';
        return <span style={{ color }}>${amount.toFixed(2)}</span>;
      },
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 120,
      render: (_: any, record: Transaction) => (
        <Space>
          <Button
            type="text"
            icon={<EditOutlined />}
            onClick={() => onEdit(record)}
            title="Edit"
          />
          <Popconfirm
            title="Are you sure you want to delete this transaction?"
            onConfirm={() => onDelete(record.id)}
            okText="Yes"
            cancelText="No"
          >
            <Button type="text" danger icon={<DeleteOutlined />} title="Delete" />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <Table
      columns={columns}
      dataSource={transactions}
      rowKey="id"
      pagination={{ 
        pageSize: 10,
        showSizeChanger: true,
        showTotal: (total) => `Total ${total} items`
      }}
      scroll={{ x: true }}
      summary={(pageData) => {
        const totalDeposit = pageData
          .filter(item => item.type === 'DEPOSIT')
          .reduce((sum, item) => sum + item.amount, 0);
        
        const totalWithdrawal = pageData
          .filter(item => item.type === 'WITHDRAWAL')
          .reduce((sum, item) => sum + item.amount, 0);
          const totalTransfer = pageData
          .filter(item => item.type === 'TRANSFER')
          .reduce((sum, item) => sum + item.amount, 0);

        return (
          <Table.Summary fixed>
            <Table.Summary.Row>
              <Table.Summary.Cell index={0} colSpan={3}>Total</Table.Summary.Cell>
              <Table.Summary.Cell index={1}>
                <span style={{ color: '#52c41a', marginRight: '15px' }}>Deposit: ${totalDeposit.toFixed(2)}</span>
                <span style={{ color: '#f5222d',marginRight: '15px' }}>Withdrawal: ${totalWithdrawal.toFixed(2)}</span>
                <strong>Transfer: ${totalTransfer.toFixed(2)}</strong>
              </Table.Summary.Cell>
              <Table.Summary.Cell index={2} colSpan={2} />
            </Table.Summary.Row>
          </Table.Summary>
        );
      }}
    />
  );
};

export default TransactionList; 