import tkinter as tk
from tkinter import ttk, filedialog, messagebox
import csv
from collections import Counter

CARDS=['A','2','3','4','5','6','7','8','9','10','J','Q','K']
ODD={'A','3','5','7','9','J','K'}
RESULTS=['D','T','TIE']
RESULT_COLORS={'D':'#21c55d','T':'#f59e0b','TIE':'#8b5cf6'}
CARD_COLORS={'A':'#ef4444','J':'#06b6d4','Q':'#a855f7','K':'#ec4899'}

class App:
    def __init__(self, root):
        self.root=root
        root.title('Dragon Tiger Analyzer - PC')
        root.geometry('1280x820'); root.minsize(1050,700)
        root.configure(bg='#111827')
        self.data=[]
        self.dr=tk.StringVar(value='A'); self.ti=tk.StringVar(value='A'); self.re=tk.StringVar(value='D')
        self.pair=tk.StringVar(value='JQ'); self.summary=tk.StringVar(value='PAIR JQ | NO HISTORY')
        self.build_styles(); self.build_ui(); self.refresh()

    def build_styles(self):
        s=ttk.Style(); s.theme_use('clam')
        s.configure('TFrame',background='#111827'); s.configure('TLabelframe',background='#111827',foreground='#e5e7eb')
        s.configure('TLabelframe.Label',background='#111827',foreground='#fbbf24',font=('Segoe UI',11,'bold'))
        s.configure('TLabel',background='#111827',foreground='#f3f4f6',font=('Segoe UI',10))
        s.configure('TButton',font=('Segoe UI',10,'bold'),padding=(12,7),background='#374151',foreground='white')
        s.map('TButton',background=[('active','#4b5563')])
        s.configure('TCombobox',fieldbackground='#1f2937',background='#374151',foreground='white')
        s.configure('Treeview',background='#1f2937',fieldbackground='#1f2937',foreground='#f9fafb',rowheight=34,font=('Segoe UI',10))
        s.configure('Treeview.Heading',background='#7f1d1d',foreground='white',font=('Segoe UI',10,'bold'),padding=8)

    def build_ui(self):
        header=tk.Frame(self.root,bg='#7f1d1d',height=70); header.pack(fill='x')
        tk.Label(header,text='DRAGON',bg='#991b1b',fg='white',font=('Segoe UI',17,'bold'),width=15,pady=12).pack(side='left',padx=(18,4),pady=10)
        tk.Label(header,text='TIE',bg='#312e81',fg='white',font=('Segoe UI',17,'bold'),width=9,pady=12).pack(side='left',padx=4,pady=10)
        tk.Label(header,text='TIGER',bg='#111827',fg='white',font=('Segoe UI',17,'bold'),width=15,pady=12).pack(side='left',padx=4,pady=10)
        tk.Label(header,text='PAIR',bg='#991b1b',fg='white',font=('Segoe UI',17,'bold'),width=15,pady=12).pack(side='left',padx=4,pady=10)
        tk.Label(header,text='PC ANALYZER',bg='#7f1d1d',fg='#fde68a',font=('Segoe UI',15,'bold')).pack(side='right',padx=20)

        tools=tk.Frame(self.root,bg='#111827'); tools.pack(fill='x',padx=18,pady=(12,5))
        for text,cmd in [('Import Excel/CSV',self.import_data),('Export Excel/CSV',self.export_data),('Add Result',self.add_result),('Clear All',self.clear_all)]:
            tk.Button(tools,text=text,command=cmd,bg='#374151',fg='white',activebackground='#4b5563',activeforeground='white',font=('Segoe UI',10,'bold'),relief='flat',padx=12,pady=7).pack(side='left',padx=4)
        tk.Label(tools,text='Historical statistical reference only',bg='#111827',fg='#9ca3af',font=('Segoe UI',9)).pack(side='right',padx=10)

        entry=tk.LabelFrame(self.root,text='  NEW ROUND  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        entry.pack(fill='x',padx=18,pady=6)
        for i,(lab,var,vals) in enumerate([('Dragon',self.dr,CARDS),('Tiger',self.ti,CARDS),('Result',self.re,RESULTS)]):
            tk.Label(entry,text=lab,bg='#111827',fg='#e5e7eb',font=('Segoe UI',10,'bold')).grid(row=0,column=i*2,padx=(12,5),pady=12)
            cb=ttk.Combobox(entry,textvariable=var,values=vals,state='readonly',width=9); cb.grid(row=0,column=i*2+1,padx=5,pady=12)
        tk.Button(entry,text='ADD ROUND',command=self.add_result,bg='#16a34a',fg='white',font=('Segoe UI',10,'bold'),relief='flat',padx=18,pady=7).grid(row=0,column=7,padx=20)

        pair=tk.LabelFrame(self.root,text='  PAIR REFERENCE / STATISTICAL PREDICTION  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        pair.pack(fill='x',padx=18,pady=6)
        tk.Label(pair,text='Pair',bg='#111827',fg='white',font=('Segoe UI',10,'bold')).pack(side='left',padx=(12,5),pady=12)
        cb=ttk.Combobox(pair,textvariable=self.pair,values=[a+b for a in CARDS for b in CARDS],width=12)
        cb.pack(side='left',padx=5); cb.bind('<<ComboboxSelected>>',lambda e:self.analyze())
        tk.Button(pair,text='ANALYZE',command=self.analyze,bg='#b91c1c',fg='white',font=('Segoe UI',10,'bold'),relief='flat',padx=14,pady=6).pack(side='left',padx=6)
        tk.Label(pair,textvariable=self.summary,bg='#111827',fg='#fde68a',font=('Segoe UI',11,'bold')).pack(side='left',padx=20)

        last=tk.LabelFrame(self.root,text='  LAST RESULT  ',bg='#111827',fg='#fbbf24',font=('Segoe UI',11,'bold'),bd=1,relief='groove')
        last.pack(fill='x',padx=18,pady=6)
        self.last_frame=tk.Frame(last,bg='#111827'); self.last_frame.pack(fill='x',padx=10,pady=8)

        table=tk.Frame(self.root,bg='#111827'); table.pack(fill='both',expand=True,padx=18,pady=(5,12))
        cols=('S NO','DRAGON','TIGER','RESULT','PAIR','DRAGON O/E','TIGER O/E','PREV RESULT')
        self.tree=ttk.Treeview(table,columns=cols,show='headings')
        widths={'S NO':75,'DRAGON':90,'TIGER':90,'RESULT':90,'PAIR':100,'DRAGON O/E':130,'TIGER O/E':130,'PREV RESULT':130}
        for c in cols: self.tree.heading(c,text=c); self.tree.column(c,width=widths[c],anchor='center')
        for tag,color in [('dragon','#14532d'),('tiger','#92400e'),('tie','#4c1d95'),('even','#1e3a8a'),('odd','#7c2d12')]: self.tree.tag_configure(tag,background=color,foreground='white')
        self.tree.pack(side='left',fill='both',expand=True)
        y=ttk.Scrollbar(table,orient='vertical',command=self.tree.yview); y.pack(side='right',fill='y'); self.tree.configure(yscrollcommand=y.set)

    def oe(self,c): return 'ODD' if c in ODD else 'EVEN'
    def tag_for(self,r): return {'D':'dragon','T':'tiger','TIE':'tie'}[r]

    def add_result(self):
        n=max([int(r['sno']) for r in self.data if str(r['sno']).isdigit()] or [0])+1
        self.data.append({'sno':n,'dragon':self.dr.get(),'tiger':self.ti.get(),'result':self.re.get()}); self.refresh()

    def clear_all(self):
        if messagebox.askyesno('Confirm','Delete all loaded rounds?'):
            self.data=[]; self.refresh()

    def refresh(self):
        for x in self.tree.get_children(): self.tree.delete(x)
        for i,r in enumerate(self.data):
            prev=self.data[i-1]['result'] if i else ''
            self.tree.insert('', 'end', values=(r['sno'],r['dragon'],r['tiger'],r['result'],r['dragon']+r['tiger'],self.oe(r['dragon']),self.oe(r['tiger']),prev),tags=(self.tag_for(r['result']),))
        for x in self.last_frame.winfo_children(): x.destroy()
        for r in self.data[-14:]:
            tk.Label(self.last_frame,text=r['result'],bg=RESULT_COLORS[r['result']],fg='white',font=('Segoe UI',10,'bold'),width=4,height=2,relief='ridge',bd=1).pack(side='right',padx=3)
        self.update_title_counts()

    def update_title_counts(self):
        c=Counter(r['result'] for r in self.data)
        self.summary.set(f'PAIR {self.pair.get().upper()} | D {c["D"]} | T {c["T"]} | TIE {c["TIE"]} | TOTAL {len(self.data)}')

    def analyze(self):
        p=self.pair.get().upper().strip(); rows=[r for r in self.data if r['dragon']+r['tiger']==p]
        cnt=Counter(r['result'] for r in rows)
        if not rows: self.summary.set(f'PAIR {p} | NO HISTORY'); return
        maxres=max(RESULTS,key=lambda x:cnt[x]); pct=cnt[maxres]/len(rows)*100
        self.summary.set(f'PAIR {p} | MATCH {len(rows)} | D {cnt["D"]} | T {cnt["T"]} | TIE {cnt["TIE"]} | MAX {maxres} ({pct:.1f}%)')
        self.tree.selection_remove(self.tree.selection())
        for item in self.tree.get_children():
            if self.tree.item(item,'values')[4]==p: self.tree.selection_add(item)

    def import_data(self):
        path=filedialog.askopenfilename(filetypes=[('Excel','*.xlsx'),('CSV','*.csv'),('All files','*.*')])
        if not path:return
        try:
            rows=[]
            if path.lower().endswith('.csv'):
                with open(path,newline='',encoding='utf-8-sig') as f: rows=list(csv.reader(f))
            else:
                from openpyxl import load_workbook
                wb=load_workbook(path,data_only=True); ws=wb.active; rows=list(ws.iter_rows(values_only=True))
            out=[]; seen=set()
            for row in rows:
                if len(row)<4: continue
                try: sno=int(row[0])
                except: continue
                d=str(row[1]).strip().upper(); t=str(row[2]).strip().upper(); res=str(row[3]).strip().upper()
                if d in CARDS and t in CARDS and res in RESULTS and sno not in seen:
                    out.append({'sno':sno,'dragon':d,'tiger':t,'result':res}); seen.add(sno)
            out.sort(key=lambda r:r['sno']); self.data=out; self.refresh(); messagebox.showinfo('Import',f'Imported {len(out)} unique rounds.')
        except Exception as e: messagebox.showerror('Import error',str(e))

    def export_data(self):
        if not self.data: messagebox.showwarning('Export','No data to export.'); return
        path=filedialog.asksaveasfilename(defaultextension='.xlsx',filetypes=[('Excel','*.xlsx'),('CSV','*.csv')])
        if not path:return
        try:
            rows=[['S NO','DRAGON','TIGER','RESULT','PAIR','DRAGON O/E','TIGER O/E','PREV RESULT']]
            for i,r in enumerate(self.data): rows.append([r['sno'],r['dragon'],r['tiger'],r['result'],r['dragon']+r['tiger'],self.oe(r['dragon']),self.oe(r['tiger']),self.data[i-1]['result'] if i else ''])
            if path.lower().endswith('.csv'):
                with open(path,'w',newline='',encoding='utf-8-sig') as f: csv.writer(f).writerows(rows)
            else:
                from openpyxl import Workbook
                wb=Workbook(); ws=wb.active; ws.title='ANALYZER'
                for row in rows: ws.append(row)
                wb.save(path)
            messagebox.showinfo('Export','Export completed successfully.')
        except Exception as e: messagebox.showerror('Export error',str(e))

if __name__=='__main__':
    root=tk.Tk(); App(root); root.mainloop()
